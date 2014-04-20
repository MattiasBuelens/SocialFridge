package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


/**
 * Party.
 */
@Entity(name = Party.KIND)
public class Party {

    public static final String KIND = "Party";

    /*
     * Load groups.
     */

    public static class Everything extends Partial {
    }

    public static class Partial {
    }

    /*
     * Comparators.
     */
    public static final Ordering<Party> dateComparator = new Ordering<Party>() {
        @Override
        public int compare(Party left, Party right) {
            return left.getDate().compareTo(right.getDate());
        }
    };

    /**
     * Party ID.
     */
    @Id
    private Long id;

    /**
     * Host.
     */
    @Load
    private Ref<User> host;

    /**
     * Party status.
     */
    private Status status = Status.INVITING;

    /**
     * Members.
     */
    @Load(Everything.class)
    private Set<Ref<PartyMember>> members = new HashSet<Ref<PartyMember>>();

    /**
     * Visible users.
     */
    @Load
    @Index
    private Set<Ref<User>> visibleUsers = new HashSet<Ref<User>>();

    /**
     * Merged time slots from partners.
     */
    private List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();

    /**
     * Party date.
     */
    @Index
    private Date date;

    /**
     * Date created.
     */
    private Date dateCreated;

    public Party() {
    }

    public Party(Long id) {
        this.id = id;
    }

    /**
     * Party ID.
     */
    public Long getID() {
        return id;
    }

    public static Key<Party> getKey(long partyID) {
        return Key.create(Party.class, partyID);
    }

    public static Ref<Party> getRef(long partyID) {
        return Ref.create(getKey(partyID));
    }

    /**
     * Host.
     */
    public String getHostID() {
        return host.getKey().getName();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public User getHost() {
        return host.get();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setHost(User host, List<TimeSlot> timeSlots) {
        // Set as host
        this.host = Ref.create(host);
        PartyMember member = new PartyMember(this, host, PartyMember.Status.HOST);
        member.setTimeSlots(timeSlots);
        // Update members and partners
        updateMember(member);
        updatePartner(member);
        // Add to party
        host.addParty(this);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<PartyMember> getHostMember() {
        return getMember(getHostID());
    }

    /**
     * Party status.
     */
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isInviting() {
        return getStatus() == Status.INVITING;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isPlanning() {
        return getStatus() == Status.PLANNING;
    }

    public void setPlanning() {
        setStatus(Status.PLANNING);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isPlanned() {
        return getStatus() == Status.PLANNED;
    }

    public void setPlanned(TimeSlot chosenTimeSlot) {
        setStatus(Status.PLANNED);
        setDate(chosenTimeSlot.getBeginDate());
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isDisbanded() {
        return getStatus() == Status.DISBANDED;
    }

    public void setDisbanded() {
        setStatus(Status.DISBANDED);
    }

    /**
     * Party date.
     * When {@link #isPlanned() not planned yet}, the time part is not yet configured and should be ignored.
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isCompleted() {
        return isPlanned() && new Date().after(getDate());
    }

    /**
     * Date created.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Members.
     */
    protected Set<Ref<PartyMember>> getMemberKeys() {
        return members;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Map<String, PartyMember> getMembersMap() {
        Map<String, PartyMember> map = new HashMap<String, PartyMember>();
        for (PartyMember member : getMembers()) {
            map.put(member.getUserID(), member);
        }
        return map;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<PartyMember> getMembers() {
        return ofy().load().refs(getMemberKeys()).values();
    }

    public Ref<PartyMember> getMember(User user) {
        return getMember(Ref.create(user));
    }

    public Ref<PartyMember> getMember(Ref<User> userRef) {
        return getMember(userRef.getKey().getName());
    }

    public Ref<PartyMember> getMember(String userID) {
        for (Ref<PartyMember> ref : getMemberKeys()) {
            if (userID.equals(ref.getKey().getName())) {
                return ref;
            }
        }
        return null;
    }

    protected PartyMember updateMember(PartyMember member) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(member.getUserID());
        if (ref != null) {
            // Copy to existing member
            PartyMember existingMember = ref.get();
            existingMember.setUserName(member.getUserName());
            existingMember.setStatus(member.getStatus());
            existingMember.setTimeSlots(member.getTimeSlots());
            member = existingMember;
        } else {
            // Add member
            members.add(Ref.create(member));
        }
        // Save member
        ofy().save().entity(member).now();
        // Update
        updateVisible(member);
        return member;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<String> getMemberIDs() {
        List<String> memberIDs = new ArrayList<String>(getMemberKeys().size());
        for (Ref<PartyMember> ref : getMemberKeys()) {
            memberIDs.add(ref.getKey().getName());
        }
        return memberIDs;
    }

    /**
     * Partners.
     */
    public Collection<PartyMember> getPartners() {
        return Collections2.filter(getMembers(), new Predicate<PartyMember>() {
            @Override
            public boolean apply(@Nullable PartyMember member) {
                return member.isInParty();
            }
        });
    }

    protected void updatePartner(PartyMember member) {
        updateTimeSlots();
    }

    /**
     * Invitees.
     */
    public Collection<PartyMember> getInvitees() {
        return Collections2.filter(getMembers(), new Predicate<PartyMember>() {
            @Override
            public boolean apply(@Nullable PartyMember member) {
                return member.isInvited();
            }
        });
    }

    /**
     * Visible users.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<Ref<User>> getVisibleUserKeys() {
        return visibleUsers;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<User> getVisibleUsers() {
        return ofy().load().refs(getVisibleUserKeys()).values();
    }

    protected void updateVisible(PartyMember member) {
        Ref<User> userRef = member.getUserRef();
        if (member.isVisible()) {
            visibleUsers.add(userRef);
        } else {
            visibleUsers.remove(userRef);
        }
    }

    /**
     * Merged time slots from partners.
     */
    public Collection<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    protected void setTimeSlots(Collection<TimeSlot> timeSlots) {
        this.timeSlots.clear();
        this.timeSlots.addAll(timeSlots);
        Collections.sort(this.timeSlots, TimeSlot.beginDateComparator);
    }

    public TimeSlot getTimeSlot(Date beginDate, Date endDate) {
        for (TimeSlot slot : getTimeSlots()) {
            if (slot.getBeginDate().equals(beginDate) && slot.getEndDate().equals(endDate)) {
                return slot;
            }
        }
        return null;
    }

    public boolean isAvailable(TimeSlot timeSlot) {
        return isAvailable(timeSlot.getBeginDate(), timeSlot.getEndDate());
    }

    public boolean isAvailable(Date beginHour, Date endHour) {
        TimeSlot slot = getTimeSlot(beginHour, endHour);
        return slot != null && slot.isAvailable();
    }

    protected void updateTimeSlots() {
        // Collect time slots from all partners
        List<TimeSlot> allSlots = new ArrayList<TimeSlot>();
        for (PartyMember partner : getPartners()) {
            allSlots.addAll(partner.getTimeSlots());
        }
        // Merge time slots
        Collection<TimeSlot> mergedSlots = TimeSlot.merge(allSlots);
        // Replace time slots
        setTimeSlots(mergedSlots);
    }

    /**
     * Invite a user to this party.
     *
     * @param invitee The user to invite.
     * @throws IllegalArgumentException If the user cannot be invited because he declined an earlier invite.
     */
    public void invite(User invitee) throws IllegalArgumentException {
        if (!isInviting()) {
            throw new IllegalArgumentException("Cannot invite user, no longer inviting.");
        }
        Ref<PartyMember> ref = getMember(invitee.getID());
        PartyMember member;
        if (ref != null) {
            member = ref.get();
            if (!member.needsInvite()) {
                // Already in party or invited, don't re-invite
                return;
            }
            if (!member.invite()) {
                // Could not invite, previously declined
                throw new IllegalArgumentException("Cannot invite user, declined an earlier invite.");
            }
        } else {
            // Add invitee
            member = new PartyMember(this, invitee, PartyMember.Status.INVITED);
            updateMember(member);
        }
        // Add to party
        invitee.addParty(this);
    }

    /**
     * Cancel an invite for a user.
     *
     * @param invitee The user of whom to cancel the invite.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public void cancelInvite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot cancel user's invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.cancelInvite()) {
            throw new IllegalArgumentException("Cannot cancel user's invite, was not invited or is already in the party.");
        }
        updateMember(member);
        // Remove from party
        invitee.removeParty(this);
    }

    /**
     * Accept a user's invite.
     *
     * @param invitee   The user of whom to accept the invite.
     * @param timeSlots The time slots chosen by the user.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public void acceptInvite(User invitee, List<TimeSlot> timeSlots) throws IllegalArgumentException {
        if (!isInviting()) {
            throw new IllegalArgumentException("Cannot accept invite, no longer inviting.");
        }
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot accept invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.acceptInvite()) {
            throw new IllegalArgumentException("Cannot accept invite, was not invited or is already in the party.");
        }
        // Set time slots
        member.setTimeSlots(timeSlots);
        updateMember(member);
        updatePartner(member);
        // Add to party (should already be added though)
        invitee.addParty(this);
    }

    /**
     * Decline a user's invite.
     *
     * @param invitee The user of whom to decline the invite.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public void declineInvite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot decline invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.declineInvite()) {
            throw new IllegalArgumentException("Cannot decline invite, was not invited or is already in the party.");
        }
        /*
         * Note: Do NOT remove from members!
         * We should never allow a user to be re-invited after he declined.
         */
        updateMember(member);
        // Remove from party
        invitee.removeParty(this);
    }

    /**
     * Make a user leave the party.
     *
     * @param invitee The user to leave.
     * @throws IllegalArgumentException If the user is the host or was not in the party.
     */
    public void leave(User invitee) throws IllegalArgumentException {
        if (!isInviting()) {
            throw new IllegalArgumentException("Cannot leave, no longer inviting.");
        }
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot leave, was not in the party.");
        }
        PartyMember member = ref.get();
        if (!member.leave()) {
            throw new IllegalArgumentException("Cannot leave, is host or was not in the party.");
        }
        updateMember(member);
        updatePartner(member);
        // Remove from party
        invitee.removeParty(this);
    }

    public static enum Status {

        /**
         * Inviting partners to party.
         */
        INVITING,

        /**
         * Planning a time for the party.
         */
        PLANNING,

        /**
         * Party planned.
         */
        PLANNED,

        /**
         * Party disbanded.
         */
        DISBANDED;

        @Override
        public String toString() {
            return super.toString();
        }

    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

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
        // Create date for time slot
        // TODO This is a dirty fix, find a better way to deal with timezone of host!
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Brussels"));
        calendar.setTime(getDate());
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, chosenTimeSlot.getBeginHour());
        // Store data
        setDate(calendar.getTime());
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
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Set<Ref<PartyMember>> getMemberKeys() {
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
        Collections.sort(this.timeSlots, new TimeSlot.BeginHourComparator());
    }

    public TimeSlot getTimeSlot(int beginHour, int endHour) {
        for (TimeSlot slot : getTimeSlots()) {
            if (slot.getBeginHour() == beginHour && slot.getEndHour() == endHour) {
                return slot;
            }
        }
        return null;
    }

    public boolean isAvailable(int beginHour, int endHour) {
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
        cancelInvite(ref.get());
    }

    protected void cancelInvite(PartyMember invitee) throws IllegalArgumentException {
        if (!invitee.cancelInvite()) {
            throw new IllegalArgumentException("Cannot cancel user's invite, was not invited or is already in the party.");
        }
        updateMember(invitee);
    }

    /**
     * Cancel all open invites.
     *
     * @return The user IDs of canceled invitees.
     */
    public List<Ref<User>> cancelInvites() {
        List<Ref<User>> canceledUserIDs = new ArrayList<Ref<User>>();
        for (PartyMember member : getMembers()) {
            if (member.isInvited()) {
                cancelInvite(member);
                canceledUserIDs.add(User.getRef(member.getUserID()));
            }
        }
        return canceledUserIDs;
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
        PLANNED;

        @Override
        public String toString() {
            return super.toString();
        }

    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
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
    @Load(Partial.class)
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
     * Partners.
     */
    @Load(Partial.class)
    private Set<Ref<PartyMember>> partners = new HashSet<Ref<PartyMember>>();

    /**
     * Merged time slots from partners.
     */
    private List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();

    /**
     * Party date.
     */
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
        updateMember(member);
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

    public boolean isInviting() {
        return getStatus() == Status.INVITING;
    }

    public boolean isArranging() {
        return getStatus() == Status.ARRANGING;
    }

    public boolean isDone() {
        return getStatus() == Status.DONE;
    }

    /**
     * Party date.
     * When {@link #isDone() not done yet}, the time part is not yet configured and should be ignored.
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
        boolean wasInParty = false;
        if (ref != null) {
            // Copy to existing member
            PartyMember existingMember = ref.get();
            wasInParty = existingMember.isInParty();
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
        // Update partners if needed
        if (wasInParty != member.isInParty()) {
            updatePartner(member);
        }
        return member;
    }

    protected void removeMember(User user) throws IllegalArgumentException {
        // Remove from party
        Ref<PartyMember> ref = getMember(user);
        if (ref != null) {
            members.remove(ref);
        }
        // Remove from user
        user.removeParty(this);
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
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Set<Ref<PartyMember>> getPartnerKeys() {
        return partners;
    }

    public Collection<PartyMember> getPartners() {
        return ofy().load().refs(getPartnerKeys()).values();
    }

    protected void updatePartner(PartyMember member) {
        Ref<PartyMember> ref = Ref.create(member);
        if (member.isInParty()) {
            partners.add(ref);
        } else {
            partners.remove(ref);
        }
        updateTimeSlots();
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
        // Remove from party
        invitee.removeParty(this);
    }

    public static enum Status {

        /**
         * Inviting partners to party.
         */
        INVITING,

        /**
         * Arranging a time for the party.
         */
        ARRANGING,

        /**
         * Party arranged.
         */
        DONE;

        @Override
        public String toString() {
            return super.toString();
        }

    }

}

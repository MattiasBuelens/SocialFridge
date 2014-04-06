package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;

import java.util.ArrayList;
import java.util.Collection;
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
    @Load(Everything.class)
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
    @Load(Everything.class)
    private Set<Ref<PartyMember>> partners = new HashSet<Ref<PartyMember>>();

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

    public void setHostID(String hostID) {
        host = Ref.create(Key.create(User.class, hostID));
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public PartyMember setHost(User host) {
        // Set as host
        this.host = Ref.create(host);
        PartyMember member = new PartyMember(this, host, PartyMember.Status.HOST);
        member = updateMember(member);
        // Add to party
        host.addParty(this);
        return member;
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
            member = existingMember;
        } else {
            // Add member
            members.add(Ref.create(member));
        }
        // Update partners
        updatePartner(member);
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
    }

    /**
     * Invite a user to this party.
     *
     * @param invitee The user to invite.
     * @return The member to be saved.
     * @throws IllegalArgumentException If the user cannot be invited because he declined an earlier invite.
     */
    public PartyMember invite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        PartyMember member;
        if (ref != null) {
            member = ref.get();
            if (!member.needsInvite()) {
                // Already in party or invited, don't re-invite
                return member;
            }
            if (!member.invite()) {
                // Could not invite, previously declined
                throw new IllegalArgumentException("Cannot invite user, declined an earlier invite.");
            }
        } else {
            // Add invitee
            member = new PartyMember(this, invitee, PartyMember.Status.INVITED);
            member = updateMember(member);
        }
        // Add to party
        invitee.addParty(this);
        return member;
    }

    /**
     * Cancel an invite for a user.
     *
     * @param invitee The user of whom to cancel the invite.
     * @return The member to be saved.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public PartyMember cancelInvite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot cancel user's invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.cancelInvite()) {
            throw new IllegalArgumentException("Cannot cancel user's invite, was not invited or is already in the party.");
        }
        // Remove from party
        invitee.removeParty(this);
        return member;
    }

    /**
     * Accept a user's invite.
     *
     * @param invitee The user of whom to accept the invite.
     * @return The member to be saved.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public PartyMember acceptInvite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot accept invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.acceptInvite()) {
            throw new IllegalArgumentException("Cannot accept invite, was not invited or is already in the party.");
        }
        // Add to party (should already be added though)
        invitee.addParty(this);
        return member;
    }

    /**
     * Decline a user's invite.
     *
     * @param invitee The user of whom to decline the invite.
     * @return The member to be saved.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public PartyMember declineInvite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot decline invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.declineInvite()) {
            throw new IllegalArgumentException("Cannot decline invite, was not invited or is already in the party.");
        }
        // Remove from party
        invitee.removeParty(this);
        /*
         * Note: Do NOT remove from members!
         * We should never allow a user to be re-invited after he declined.
         */
        return member;
    }

    /**
     * Make a user leave the party.
     *
     * @param invitee The user to leave.
     * @return The member to be saved.
     * @throws IllegalArgumentException If the user is the host or was not in the party.
     */
    public PartyMember leave(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        if (ref == null) {
            throw new IllegalArgumentException("Cannot leave, was not in the party.");
        }
        PartyMember member = ref.get();
        if (!member.leave()) {
            throw new IllegalArgumentException("Cannot leave, is host or was not in the party.");
        }
        // Remove from party
        invitee.removeParty(this);
        return member;
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

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

    @Id
    private Long id;

    @Load(Everything.class)
    private Ref<User> host;

    @Load(Everything.class)
    private Set<Ref<PartyMember>> members = new HashSet<Ref<PartyMember>>();

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

    public Collection<PartyMember> getMembers() {
        return ofy().load().refs(getMemberKeys()).values();
    }

    public void setMembers(Collection<PartyMember> newMembers) {
        members.clear();
        for (PartyMember member : newMembers) {
            members.add(Ref.create(member));
        }
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

    public boolean hasMember(String userID) {
        return getMember(userID) != null;
    }

    protected PartyMember updateMember(PartyMember member) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(member.getUserID());
        if (ref != null) {
            // Copy to existing member
            PartyMember existingMember = ref.get();
            existingMember.setUserName(member.getUserName());
            existingMember.setStatus(member.getStatus());
            return existingMember;
        } else {
            // Add member
            members.add(Ref.create(member));
            return member;
        }
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
     * Invite a user to this party.
     *
     * @param invitee The user to invite.
     * @return The invited member.
     * @throws IllegalArgumentException If the user cannot be invited because he declined an earlier invite.
     */
    public PartyMember invite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee.getID());
        PartyMember member;
        if (ref != null) {
            member = ref.get();
            if (member.isInParty()) {
                // Already in the party
                return member;
            }
            if (member.isInvited()) {
                // Already invited, don't re-invite
                return member;
            }
            if (!member.canInvite()) {
                // Previously declined
                throw new IllegalArgumentException("Cannot invite user, declined an earlier invite.");
            }
            // Invite
            member.setStatus(PartyMember.Status.INVITED);
        } else {
            // Add invitee
            member = new PartyMember(this, invitee, PartyMember.Status.INVITED);
            member = updateMember(member);
        }
        // Add to party
        invitee.addParty(this);
        return member;
    }

}

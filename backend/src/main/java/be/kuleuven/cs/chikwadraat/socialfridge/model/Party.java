package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * Party.
 */
@Entity(name = Party.KIND)
public class Party {

    public static final String KIND = "Party";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hostID;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "party")
    private List<PartyMember> members;

    public Party() {
        this.members = new ArrayList<PartyMember>();
    }

    public Party(Long id, User host) {
        this();
        this.id = id;
        setHost(host);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key getKey() {
        return getKey(getID());
    }

    public static Key getKey(long id) {
        return KeyFactory.createKey(KIND, id);
    }

    /**
     * Party ID.
     */
    public Long getID() {
        return id;
    }

    public static long getID(Key partyKey) {
        return partyKey.getId();
    }

    /**
     * Host.
     */
    public String getHostID() {
        return hostID;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setHost(User host) {
        this.hostID = host.getID();
        addMember(new PartyMember(this, host, PartyMember.Status.HOST));
    }

    /**
     * Members.
     */
    public List<PartyMember> getMembers() {
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

    public PartyMember getMember(String userID) {
        for (PartyMember member : getMembers()) {
            if (userID.equals(member.getUserID()))
                return member;
        }
        return null;
    }

    public boolean hasMember(String userID) {
        return getMember(userID) != null;
    }

    public void addMember(PartyMember member) throws IllegalArgumentException {
        if (hasMember(member.getUserID())) {
            throw new IllegalArgumentException("Party member already exists");
        }
        getMembers().add(member);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<String> getMemberIDs() {
        List<String> memberIDs = new ArrayList<String>(getMembers().size());
        for (PartyMember member : getMembers()) {
            memberIDs.add(member.getUserID());
        }
        return memberIDs;
    }

    /**
     * Invite a user to this party.
     *
     * @param invitee The user to invite.
     * @return True iff the user was invited.
     * @throws IllegalArgumentException If the user cannot be invited because he declined an earlier invite.
     */
    public boolean invite(User invitee) throws IllegalArgumentException {
        PartyMember member = getMember(invitee.getID());
        if (member != null) {
            if (member.isInParty()) {
                // Already in the party
                return false;
            }
            if (member.isInvited()) {
                // Already invited, don't re-invite
                return false;
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
            addMember(member);
        }
        return true;
    }

}

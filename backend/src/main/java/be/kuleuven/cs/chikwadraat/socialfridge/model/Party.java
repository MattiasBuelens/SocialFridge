package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Party.
 */
@Entity(name = Party.KIND)
public class Party {

    public static final String KIND = "Party";

    @Id
    private Long id;
    private User host;
    private List<PartyMember> members;

    public Party() {
    }

    public Party(long id, User host) {
        this.id = id;
        this.members = new ArrayList<PartyMember>();
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
    public User getHost() {
        return host;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getHostID() {
        return getHost().getID();
    }

    protected void setHost(User host) {
        this.host = host;
        addMember(new PartyMember(this, host, PartyMember.Status.HOST));
    }

    /**
     * Members.
     */
    public List<PartyMember> getMembers() {
        return members;
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

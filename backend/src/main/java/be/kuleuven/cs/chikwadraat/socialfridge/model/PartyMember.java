package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.datanucleus.api.jpa.annotations.Extension;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Party member.
 */
@Entity(name = PartyMember.KIND)
public class PartyMember {

    public static final String KIND = "PartyMember";

    /**
     * User ID.
     */
    @Id
    private String userID;

    /**
     * User name.
     */
    private String userName;

    /**
     * Party.
     */
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Party.class)
    private Party party;

    /**
     * Member status.
     */
    private Status status;

    /**
     * Key of parent party.
     */
    @Extension(vendorName = "datanucleus", key = "gae.parent-pk", value = "true")
    private Key partyKey;

    public PartyMember() {
    }

    public PartyMember(Party party, User user, Status status) {
        checkNotNull(party);
        checkNotNull(user);
        checkNotNull(status);
        this.party = party;
        this.partyKey = party.getKey();
        this.userName = user.getName();
        this.userID = user.getID();
        this.status = status;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key getKey() {
        return getKey(getParty(), getUserID());
    }

    public static Key getKey(Party party, String userID) {
        return getKey(party.getKey(), userID);
    }

    public static Key getKey(long partyID, String userID) {
        return getKey(Party.getKey(partyID), userID);
    }

    public static Key getKey(Key partyKey, String userID) {
        return KeyFactory.createKey(partyKey, KIND, userID);
    }

    /**
     * Party.
     */
    public Party getParty() {
        return party;
    }

    /**
     * User ID.
     */
    public String getUserID() {
        return userID;
    }

    /**
     * User name.
     */
    public String getUserName() {
        return userName;
    }


    /**
     * Member status.
     */
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = checkNotNull(status);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isHost() {
        return getStatus() == Status.HOST;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isInvited() {
        return getStatus() == Status.INVITED;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isInParty() {
        Status status = getStatus();
        return status == Status.HOST || status == Status.ACCEPTED;
    }

    public boolean canInvite() {
        Status status = getStatus();
        return !isInParty() && status != Status.DECLINED;
    }

    /**
     * Cancel this member's invite.
     *
     * @return True iff invite was cancelled.
     */
    public boolean cancelInvite() {
        if (isInParty()) {
            // Already in the party
            return false;
        }
        if (!isInvited()) {
            // Not invited
            return false;
        }
        // Cancel invite
        setStatus(Status.INVITE_CANCELLED);
        return true;
    }

    /**
     * Accept this member's invite.
     *
     * @return True iff invite was accepted.
     */
    public boolean acceptInvite() {
        if (isInParty()) {
            // Already in the party
            return false;
        }
        if (!isInvited()) {
            // Not invited
            return false;
        }
        // Accept invite
        setStatus(Status.ACCEPTED);
        return true;
    }

    /**
     * Decline this member's invite.
     *
     * @return True iff invite was declined.
     */
    public boolean declineInvite() {
        if (isInParty()) {
            // Already in the party
            return false;
        }
        if (!isInvited()) {
            // Not invited
            return false;
        }
        // Decline invite
        setStatus(Status.DECLINED);
        return true;
    }

    /**
     * Leave the party.
     *
     * @return True iff member left the party.
     */
    public boolean leave() {
        if (!isInParty()) {
            // Not in the party
            return false;
        }
        if (isHost()) {
            // Host cannot leave party
            return false;
        }
        // Leave
        setStatus(Status.LEFT);
        return true;
    }

    public enum Status {

        /**
         * Party host.
         */
        HOST,

        /**
         * Invited to party, awaiting response.
         */
        INVITED,

        /**
         * Invite cancelled.
         */
        INVITE_CANCELLED,

        /**
         * Invite accepted, active member.
         */
        ACCEPTED,

        /**
         * Invite declined.
         */
        DECLINED,

        /**
         * Left the party.
         */
        LEFT

    }

}

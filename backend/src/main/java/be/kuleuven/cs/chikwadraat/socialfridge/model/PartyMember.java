package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.ArrayList;
import java.util.List;

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
    @Parent
    private Ref<Party> party;

    /**
     * Member status.
     */
    private Status status;

    /**
     * Chosen time slots.
     */
    private List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();

    public PartyMember() {
    }

    public PartyMember(Party party, User user, Status status) {
        checkNotNull(party);
        checkNotNull(user);
        checkNotNull(status);
        this.party = Ref.create(party);
        this.userID = user.getID();
        this.userName = user.getName();
        this.status = status;
    }

    /**
     * Party.
     */
    public long getPartyID() {
        return party.getKey().getId();
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

    public void setUserName(String userName) {
        this.userName = userName;
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

    public boolean isHost() {
        return getStatus() == Status.HOST;
    }

    public boolean isInvited() {
        return getStatus() == Status.INVITED;
    }

    public boolean isInParty() {
        return getStatus().isInParty();
    }

    public boolean canInvite() {
        return getStatus().canInvite();
    }

    public boolean needsInvite() {
        return !(isInParty() || isInvited());
    }

    /**
     * Time slots.
     */
    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public TimeSlot getTimeSlot(int beginHour, int endHour) {
        for(TimeSlot slot : getTimeSlots()) {
            if(slot.getBeginHour() == beginHour && slot.getEndHour() == endHour) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Invite this member.
     *
     * @return True iff invited.
     */
    public boolean invite() {
        if (!needsInvite()) {
            // Already in party or invited
            return false;
        }
        if (!canInvite()) {
            // Cannot invite
            return false;
        }
        // Invite
        setStatus(Status.INVITED);
        return true;
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

    public static enum Status {

        /**
         * Party host.
         */
        HOST(true, false),

        /**
         * Candidate for invitation. Should never be persisted.
         */
        CANDIDATE(false, true),

        /**
         * Invited to party, awaiting response.
         */
        INVITED(false, false),

        /**
         * Invite cancelled.
         */
        INVITE_CANCELLED(false, true),

        /**
         * Invite accepted, active member.
         */
        ACCEPTED(true, false),

        /**
         * Invite declined.
         */
        DECLINED(false, false),

        /**
         * Left the party.
         */
        LEFT(false, true);

        private boolean inParty;
        private boolean canInvite;

        private Status(boolean inParty, boolean canInvite) {
            this.inParty = inParty;
            this.canInvite = canInvite;
        }

        public boolean isInParty() {
            return inParty;
        }

        public boolean canInvite() {
            return canInvite;
        }

    }

}

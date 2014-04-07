package be.kuleuven.cs.chikwadraat.socialfridge;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

/**
 * Created by Mattias on 7/04/2014.
 */
public class Messages {

    private MessagingHelper helper;

    public static final String ARG_TYPE = "type";
    public static final String ARG_PARTY_ID = "party_id";
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_UPDATE_REASON = "party_update_reason";
    public static final String ARG_UPDATE_USER_ID = "party_update_user_id";
    public static final String ARG_UPDATE_USER_NAME = "party_update_user_name";

    public enum MessageType {
        PARTY_UPDATE("party_update"),
        PARTY_INVITE("party_invite"),
        PARTY_CANCEL_INVITE("party_cancel_invite");

        private final String name;

        private MessageType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public enum PartyUpdateReason {
        JOINED("partner_joined"), LEFT("partner_left"), DONE("done");

        private final String name;

        private PartyUpdateReason(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public Messages() {
        this.helper = new MessagingHelper();
    }

    public void partyPartnerJoined(long partyID, User joinedPartner, List<User> members) {
        partyUserUpdated(partyID, PartyUpdateReason.JOINED, joinedPartner, members);
    }

    public void partyPartnerLeft(long partyID, User leftPartner, List<User> members) {
        partyUserUpdated(partyID, PartyUpdateReason.JOINED, leftPartner, members);
    }

    public void partyUserUpdated(long partyID, PartyUpdateReason reason, User user, List<User> members) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(ARG_UPDATE_USER_ID, user.getID());
        data.put(ARG_UPDATE_USER_NAME, user.getName());
        partyUpdate(partyID, reason, data, members);
    }

    public void partyDone(long partyID, List<User> members) {
        Map<String, String> data = new HashMap<String, String>();
        partyUpdate(partyID, PartyUpdateReason.DONE, data, members);
    }

    protected void partyUpdate(long partyID, PartyUpdateReason reason, Map<String, String> data, List<User> members) {
        String collapseKey = "party_" + partyID;
        data.put(ARG_PARTY_ID, Long.toString(partyID));
        data.put(ARG_UPDATE_REASON, reason.getName());
        sendMessage(MessageType.PARTY_UPDATE, collapseKey, data, members);
    }

    public void partyInvited(long partyID, User invitee) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(ARG_PARTY_ID, Long.toString(partyID));
        data.put(ARG_USER_ID, invitee.getID());
        sendMessage(MessageType.PARTY_INVITE, null, data, invitee);
    }

    public void partyInviteCanceled(long partyID, User invitee) {
        Map<String, String> data = new HashMap<String, String>();
        data.put(ARG_PARTY_ID, Long.toString(partyID));
        data.put(ARG_USER_ID, invitee.getID());
        sendMessage(MessageType.PARTY_CANCEL_INVITE, null, data, invitee);
    }

    protected void sendMessage(MessageType type, String collapseKey, Map<String, String> data, User recipient) {
        sendMessage(type, collapseKey, data, Collections.singletonList(recipient));
    }

    protected void sendMessage(MessageType type, String collapseKey, Map<String, String> data, List<User> recipients) {
        data.put(ARG_TYPE, type.getName());
        helper.sendMessage(collapseKey, data, recipients);
    }

}

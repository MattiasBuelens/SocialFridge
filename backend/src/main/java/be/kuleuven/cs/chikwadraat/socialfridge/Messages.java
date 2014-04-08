package be.kuleuven.cs.chikwadraat.socialfridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserMessage;

/**
 * Created by Mattias on 7/04/2014.
 */
public class Messages {

    public static final String ARG_TYPE = "type";
    public static final String ARG_PARTY_ID = "party_id";
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_UPDATE_REASON = "party_update_reason";
    public static final String ARG_UPDATE_USER_ID = "party_update_user_id";
    public static final String ARG_UPDATE_USER_NAME = "party_update_user_name";

    private Messages() {
    }

    public enum MessageType {
        PARTY_UPDATE("party_update", true),
        PARTY_INVITE("party_invite", false),
        PARTY_CANCEL_INVITE("party_cancel_invite", false);

        private final String name;
        private final boolean collapsed;

        private MessageType(String name, boolean collapsed) {
            this.name = name;
            this.collapsed = collapsed;
        }

        public String getName() {
            return name;
        }

        public boolean isCollapsed() {
            return collapsed;
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

    protected static class Builder<T extends Builder> {

        private final MessageType type;
        protected final Map<String, String> data = new HashMap<String, String>();
        private final List<User> recipients = new ArrayList<User>();

        protected Builder(MessageType type) {
            this.type = type;
        }

        public T recipients(User... newRecipients) {
            return recipients(Arrays.asList(newRecipients));
        }

        public T recipients(Collection<User> newRecipients) {
            recipients.addAll(newRecipients);
            return (T) this;
        }

        public List<UserMessage> build() {
            data.put(ARG_TYPE, type.getName());
            String collapseKey = type.isCollapsed() ? type.getName() : null;
            List<UserMessage> results = new ArrayList<UserMessage>();
            for (User recipient : recipients) {
                results.add(new UserMessage(recipient, collapseKey, data));
            }
            return results;
        }

    }

    public static class PartyBuilder<T extends PartyBuilder> extends Builder<T> {

        private final long partyID;

        protected PartyBuilder(MessageType type, long partyID) {
            super(type);
            this.partyID = partyID;
        }

        public List<UserMessage> build() {
            data.put(ARG_PARTY_ID, Long.toString(partyID));
            return super.build();
        }

    }

    public static class PartyUpdateBuilder extends PartyBuilder<PartyUpdateBuilder> {

        private PartyUpdateReason reason;
        private User reasonUser;

        protected PartyUpdateBuilder(long partyID) {
            super(MessageType.PARTY_UPDATE, partyID);
        }

        public PartyUpdateBuilder reason(PartyUpdateReason reason) {
            this.reason = reason;
            return this;
        }

        public PartyUpdateBuilder reasonUser(User reasonUser) {
            this.reasonUser = reasonUser;
            return this;
        }

        public List<UserMessage> build() {
            if (reason != null) {
                data.put(ARG_UPDATE_REASON, reason.getName());
                if (reasonUser != null) {
                    data.put(ARG_UPDATE_USER_ID, reasonUser.getID());
                    data.put(ARG_UPDATE_USER_NAME, reasonUser.getName());
                }
            }
            return super.build();
        }

    }

    public static class PartyInviteBuilder extends PartyBuilder<PartyInviteBuilder> {

        private User invitee;

        protected PartyInviteBuilder(MessageType type, long partyID) {
            super(type, partyID);
        }

        public PartyInviteBuilder invitee(User invitee) {
            this.invitee = invitee;
            return this;
        }

        public List<UserMessage> build() {
            data.put(ARG_USER_ID, invitee.getID());
            return super.build();
        }

    }

    public static PartyUpdateBuilder partyUpdated(long partyID) {
        return new PartyUpdateBuilder(partyID);
    }

    public static PartyInviteBuilder partyInvited(long partyID) {
        return new PartyInviteBuilder(MessageType.PARTY_INVITE, partyID);
    }

    public static PartyInviteBuilder partyInviteCanceled(long partyID) {
        return new PartyInviteBuilder(MessageType.PARTY_CANCEL_INVITE, partyID);
    }

}

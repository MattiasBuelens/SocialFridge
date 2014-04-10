package be.kuleuven.cs.chikwadraat.socialfridge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants;
import be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageType;
import be.kuleuven.cs.chikwadraat.socialfridge.messaging.PartyUpdateReason;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserMessage;

/**
 * Created by Mattias on 7/04/2014.
 */
public class Messages {

    private Messages() {
    }

    protected static class Builder<T extends Builder> {

        private final MessageType type;
        private final String collapseKey;
        private final Map<String, String> data = new HashMap<String, String>();
        private final List<User> recipients = new ArrayList<User>();

        protected Builder(MessageType type, String collapseKey) {
            this.type = type;
            this.collapseKey = collapseKey;
        }

        public T recipients(User... newRecipients) {
            return recipients(Arrays.asList(newRecipients));
        }

        @SuppressWarnings("unchecked")
        public T recipients(Collection<User> newRecipients) {
            recipients.addAll(newRecipients);
            return (T) this;
        }

        protected final void put(String key, String value) {
            data.put(key, value);
        }

        public List<UserMessage> build() {
            put(MessageConstants.ARG_TYPE, type.getName());
            List<UserMessage> results = new ArrayList<UserMessage>();
            for (User recipient : recipients) {
                results.add(new UserMessage(recipient, collapseKey, data));
            }
            return results;
        }

    }

    public static class PartyBuilder<T extends PartyBuilder> extends Builder<T> {

        private final long partyID;
        private final User host;

        protected PartyBuilder(MessageType type, String collapseKey, Party party) {
            super(type, collapseKey);
            this.partyID = party.getID();
            this.host = party.getHost();
        }

        public List<UserMessage> build() {
            put(MessageConstants.ARG_PARTY_ID, Long.toString(partyID));
            put(MessageConstants.ARG_HOST_USER_ID, host.getID());
            put(MessageConstants.ARG_HOST_USER_NAME, host.getName());
            return super.build();
        }

    }

    public static class PartyUpdateBuilder extends PartyBuilder<PartyUpdateBuilder> {

        private PartyUpdateReason reason;
        private User reasonUser;

        protected PartyUpdateBuilder(Party party) {
            super(MessageType.PARTY_UPDATE, getCollapseKey(party.getID()), party);
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
                put(MessageConstants.ARG_UPDATE_REASON, reason.getName());
                if (reasonUser != null) {
                    put(MessageConstants.ARG_REASON_USER_ID, reasonUser.getID());
                    put(MessageConstants.ARG_REASON_USER_NAME, reasonUser.getName());
                }
            }
            return super.build();
        }

        private static String getCollapseKey(long partyID) {
            return MessageType.PARTY_UPDATE.getName() + "#" + partyID;
        }

    }

    public static class PartyInviteBuilder extends PartyBuilder<PartyInviteBuilder> {

        private User invitee;

        protected PartyInviteBuilder(MessageType type, Party party) {
            super(type, null, party);
        }

        public PartyInviteBuilder invitee(User invitee) {
            this.invitee = invitee;
            return this;
        }

        public List<UserMessage> build() {
            put(MessageConstants.ARG_INVITEE_USER_ID, invitee.getID());
            return super.build();
        }

    }

    public static PartyUpdateBuilder partyUpdated(Party party) {
        return new PartyUpdateBuilder(party);
    }

    public static PartyInviteBuilder partyInvited(Party party) {
        return new PartyInviteBuilder(MessageType.PARTY_INVITE, party);
    }

    public static PartyInviteBuilder partyInviteCanceled(Party party) {
        return new PartyInviteBuilder(MessageType.PARTY_CANCEL_INVITE, party);
    }

}

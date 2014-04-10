package be.kuleuven.cs.chikwadraat.socialfridge.messaging;

public class MessageConstants {

    /**
     * Message type (string)
     * Value set by {@link MessageType#getName()}
     */
    public static final String ARG_TYPE = "type";

    /**
     * Party ID (long)
     */
    public static final String ARG_PARTY_ID = "party_id";

    /**
     * User ID of party host (string)
     */
    public static final String ARG_HOST_USER_ID = "host_user_id";

    /**
     * User name of party host (string)
     */
    public static final String ARG_HOST_USER_NAME = "host_user_name";

    /**
     * Reason for party update (string)
     * Set when type is {@link MessageType#PARTY_UPDATE}
     * Value set by {@link PartyUpdateReason#getName()}
     */
    public static final String ARG_UPDATE_REASON = "update_reason";

    /**
     * User ID associated with party update reason (string)
     * Set when reason is {@link PartyUpdateReason#JOINED} or {@link PartyUpdateReason#LEFT}
     */
    public static final String ARG_REASON_USER_ID = "update_user_id";

    /**
     * User name associated with party update reason (string)
     * Set when reason is {@link PartyUpdateReason#JOINED} or {@link PartyUpdateReason#LEFT}
     */
    public static final String ARG_REASON_USER_NAME = "update_user_name";

    /**
     * User ID of invited user (string)
     * Set when type is {@link MessageType#PARTY_INVITE} or {@link MessageType#PARTY_CANCEL_INVITE}
     */
    public static final String ARG_INVITEE_USER_ID = "invitee_user_id";

}

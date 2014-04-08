package be.kuleuven.cs.chikwadraat.socialfridge.messaging;

/**
 * Message types.
 */
public enum MessageType {

    PARTY_UPDATE("party_update"),
    PARTY_INVITE("party_invite"),
    PARTY_CANCEL_INVITE("party_cancel_invite");

    private final String name;

    MessageType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MessageType byName(String name) {
        for (MessageType type : values()) {
            if (type.getName().equals(name))
                return type;
        }
        return null;
    }

}

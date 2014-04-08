package be.kuleuven.cs.chikwadraat.socialfridge.messaging;

/**
 * Message types.
 */
public enum MessageType {

    PARTY_UPDATE("party_update", true),
    PARTY_INVITE("party_invite", false),
    PARTY_CANCEL_INVITE("party_cancel_invite", false);

    private final String name;
    private final boolean collapsed;

    MessageType(String name, boolean collapsed) {
        this.name = name;
        this.collapsed = collapsed;
    }

    public String getName() {
        return name;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public static MessageType byName(String name) {
        for (MessageType type : values()) {
            if (type.getName().equals(name))
                return type;
        }
        return null;
    }

}

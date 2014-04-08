package be.kuleuven.cs.chikwadraat.socialfridge.messaging;

/**
 * Reasons for party updates.
 */
public enum PartyUpdateReason {

    JOINED("partner_joined"), LEFT("partner_left"), DONE("done");

    private final String name;

    PartyUpdateReason(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PartyUpdateReason byName(String name) {
        for (PartyUpdateReason reason : values()) {
            if (reason.getName().equals(name))
                return reason;
        }
        return null;
    }

}
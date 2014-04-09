package be.kuleuven.cs.chikwadraat.socialfridge.party;

import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Utility methods for working with parties.
 * <p>
 * Google doesn't let us extend {@link Party}, so we need to use static extensions...
 * </p>
 */
public class PartyUtils {

    private PartyUtils() {
    }

    /**
     * Checks whether the given user is the host of a party.
     *
     * @param party The party.
     * @param user  The user to check.
     * @return True iff the user's ID equals the party's host user ID.
     */
    public static boolean isHost(Party party, User user) {
        return isHost(party, user.getId());
    }

    /**
     * Checks whether the given user is the host of a party.
     *
     * @param party  The party.
     * @param userID The user ID to check.
     * @return True iff the user ID equals the party's host user ID.
     */
    public static boolean isHost(Party party, String userID) {
        return party.getHostID().equals(userID);
    }

    /**
     * Checks whether the given user is in the party.
     *
     * @param party The party.
     * @param user  The user to check.
     * @return True iff the user's ID occurs as user ID of some partner.
     */
    public static boolean isInParty(Party party, User user) {
        return isInParty(party, user.getId());
    }

    /**
     * Checks whether the given user is in the party.
     *
     * @param party  The party.
     * @param userID The user ID to check.
     * @return True iff the user ID occurs as user ID of some partner.
     */
    public static boolean isInParty(Party party, String userID) {
        for (PartyMember partner : party.getPartners()) {
            if (partner.getUserID().equals(userID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the host of a party.
     *
     * @param party The party.
     * @return The host.
     */
    public static PartyMember getHost(Party party) {
        String hostID = party.getHostID();
        for (PartyMember partner : party.getPartners()) {
            if (partner.getUserID().equals(hostID)) {
                return partner;
            }
        }
        return null;
    }

}

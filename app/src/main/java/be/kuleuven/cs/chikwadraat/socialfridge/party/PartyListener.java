package be.kuleuven.cs.chikwadraat.socialfridge.party;

import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Created by Mattias on 5/04/2014.
 */
public interface PartyListener {

    /**
     * Triggered when the party has been (re)loaded.
     *
     * @param party The loaded party.
     * @param user  The logged in user.
     */
    public void onPartyLoaded(Party party, User user);

    /**
     * Triggered when the party has been unloaded.
     */
    public void onPartyUnloaded();

}

package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;

/**
 * Retrieves or creates a party.
 */
public class PartyLoader extends BaseLoader<Party> {

    private static final String TAG = "PartyLoader";

    private final long partyID;

    public PartyLoader(Context context, long partyID) {
        super(context);
        this.partyID = partyID;
    }

    public long getPartyID() {
        return partyID;
    }

    @Override
    public Party loadInBackground() {
        Parties parties = Endpoints.parties(getContext());
        Session session = Session.getActiveSession();

        try {
            return parties.getParty(partyID, session.getAccessToken()).execute();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

}

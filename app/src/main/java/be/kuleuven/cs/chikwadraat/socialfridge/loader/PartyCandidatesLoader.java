package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember;

/**
 * Retrieves candidates for a party.
 */
public class PartyCandidatesLoader extends BaseLoader<List<PartyMember>> {

    private static final String TAG = "PartyCandidatesLoader";

    private final long partyID;

    public PartyCandidatesLoader(Context context, long partyID) {
        super(context);
        this.partyID = partyID;
    }

    public long getPartyID() {
        return partyID;
    }

    @Override
    public List<PartyMember> loadInBackground() {
        Parties parties = Endpoints.parties();
        Session session = Session.getActiveSession();

        try {
            return parties.getCandidates(partyID, session.getAccessToken()).execute().getItems();
        } catch (IOException e) {
            Log.e(TAG, "Error while loading candidates: " + e.getMessage());
            trackException(e);
            return null;
        }
    }

}

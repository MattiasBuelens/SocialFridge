package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;

/**
 * Retrieves or creates a party.
 */
public class PartyLoader extends BaseLoader<Party> {

    private static final String TAG = "PartyLoader";

    private Long partyID;
    private final String userID;

    public PartyLoader(Context context, Long partyID, String userID) {
        super(context);
        this.partyID = partyID;
        this.userID = userID;
    }

    public Long getPartyID() {
        return partyID;
    }

    public String getUserID() {
        return userID;
    }

    @Override
    public Party loadInBackground() {
        Parties.Builder builder = new Parties.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
        Parties endpoint = Endpoints.prepare(builder, getContext()).build();

        Session session = Session.getActiveSession();
        Party party;

        if (partyID == null) {
            // Create party
            party = new Party();
            party.setHostID(getUserID());
            try {
                party = endpoint.insertParty(session.getAccessToken(), party).execute();
                partyID = party.getId();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        } else {
            // Get party
            try {
                party = endpoint.getParty(partyID, session.getAccessToken()).execute();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }

        return party;
    }

}

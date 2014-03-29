package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
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
public class PartyLoader extends AsyncTaskLoader<Party> {

    private static final String TAG = "PartyLoader";

    private Long partyID;
    private final String userID;

    private Party party;

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

    @Override
    protected void onStartLoading() {
        if (party != null) {
            deliverResult(party);
        }
        if (takeContentChanged() || party == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(Party party) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }

        Party oldParty = this.party;
        this.party = party;

        if (isStarted()) {
            super.deliverResult(party);
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        this.party = null;
    }

}

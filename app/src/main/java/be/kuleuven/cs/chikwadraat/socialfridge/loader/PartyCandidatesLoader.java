package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.facebook.Session;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;

/**
 * Retrieves candidates for a party.
 */
public class PartyCandidatesLoader extends AsyncTaskLoader<List<PartyMember>> {

    private static final String TAG = "PartyCandidatesLoader";

    private final long partyID;

    private List<PartyMember> candidates;

    public PartyCandidatesLoader(Context context, long partyID) {
        super(context);
        this.partyID = partyID;
    }

    public long getPartyID() {
        return partyID;
    }

    @Override
    public List<PartyMember> loadInBackground() {
        Parties.Builder builder = new Parties.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
        Parties endpoint = Endpoints.prepare(builder, getContext()).build();

        Session session = Session.getActiveSession();
        try {
            return endpoint.getCandidates(partyID, session.getAccessToken()).execute().getItems();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        if (candidates != null) {
            deliverResult(candidates);
        }
        if (takeContentChanged() || candidates == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(List<PartyMember> candidates) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            return;
        }

        List<PartyMember> oldCandidates = this.candidates;
        this.candidates = candidates;

        if (isStarted()) {
            super.deliverResult(candidates);
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

        this.candidates = null;
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
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

}

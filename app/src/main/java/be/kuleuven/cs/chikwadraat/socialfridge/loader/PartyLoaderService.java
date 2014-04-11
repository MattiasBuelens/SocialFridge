package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.LongSparseArray;

import com.facebook.Session;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseIntentService;
import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;

/**
 * Created by Mattias on 11/04/2014.
 */
public class PartyLoaderService extends BaseIntentService {

    private static final String TAG = "PartyLoaderService";

    public static final String ACTION_PARTY_LOAD = "party_load";
    public static final String ACTION_PARTY_RELOAD = "party_reload";
    public static final String ACTION_PARTY_INVALIDATE = "party_invalidate";
    public static final String EXTRA_PARTY_ID = "party_id";

    public static final String ACTION_PARTY_UPDATE = "party_update";
    public static final String EXTRA_PARTY_OBJECT = "party_object";

    /*
     * Static cache.
     */
    private static LongSparseArray<Party> cache = new LongSparseArray<Party>();

    public PartyLoaderService() {
        super("PartyLoaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        long partyID = intent.getLongExtra(EXTRA_PARTY_ID, 0);
        if (action.equals(ACTION_PARTY_LOAD)) {
            // Load party if needed
            loadAndBroadcast(partyID);
        } else if (action.equals(ACTION_PARTY_INVALIDATE)) {
            // Invalidate cached party
            invalidateParty(partyID);
        } else if (action.equals(ACTION_PARTY_RELOAD)) {
            // Invalidate and load
            invalidateParty(partyID);
            loadAndBroadcast(partyID);
        }
    }

    private Party loadParty(long partyID) {
        Party party = cache.get(partyID);
        if (party != null)
            return party;

        Endpoint.Parties parties = Endpoints.parties(this);
        Session session = Session.getActiveSession();
        if (session == null || !session.isOpened())
            return null;

        try {
            party = new Party(parties.getParty(partyID, session.getAccessToken()).execute());
            cache.put(partyID, party);
            return party;
        } catch (IOException e) {
            trackException(TAG, e);
            return null;
        }
    }

    private void broadcastParty(Party party) {
        Intent updateIntent = new Intent(ACTION_PARTY_UPDATE);
        updateIntent.putExtra(EXTRA_PARTY_ID, party.getID());
        updateIntent.putExtra(EXTRA_PARTY_OBJECT, party);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }

    private void loadAndBroadcast(long partyID) {
        Party party = loadParty(partyID);
        if (party != null) {
            broadcastParty(party);
        }
    }

    private void invalidateParty(long partyID) {
        cache.delete(partyID);
    }

}

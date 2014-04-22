package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.facebook.Session;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
    public static final String EXTRA_PARTY_ID = "party_id";

    public static final String ACTION_PARTY_UPDATE = "party_update";
    public static final String EXTRA_PARTY_OBJECT = "party_object";

    /*
     * Static cache.
     */
    private static final Cache<Long, Party> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5000, TimeUnit.SECONDS)
            .maximumSize(20)
            .build();

    public PartyLoaderService() {
        super("PartyLoaderService");
    }

    public static void startLoad(Context context, long partyID) {
        Intent loadIntent = new Intent(context, PartyLoaderService.class);
        loadIntent.setAction(PartyLoaderService.ACTION_PARTY_LOAD);
        loadIntent.putExtra(PartyLoaderService.EXTRA_PARTY_ID, partyID);
        context.startService(loadIntent);
    }

    public static void startReload(Context context, long partyID) {
        invalidateParty(partyID);
        startLoad(context, partyID);
    }

    public static void invalidateParty(long partyID) {
        cache.invalidate(partyID);
    }

    public static void cacheParty(Party party) {
        cache.put(party.getID(), party);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        long partyID = intent.getLongExtra(EXTRA_PARTY_ID, 0);
        if (action.equals(ACTION_PARTY_LOAD)) {
            // Load party if needed
            loadAndBroadcast(partyID);
        }
    }

    private Party loadParty(long partyID) {
        Party party = cache.getIfPresent(partyID);
        if (party != null)
            return party;

        Endpoint.Parties parties = Endpoints.parties();
        Session session = Session.getActiveSession();
        if (session == null || !session.isOpened())
            return null;

        try {
            party = new Party(parties.getParty(partyID, session.getAccessToken()).execute());
            cacheParty(party);
            return party;
        } catch (IOException e) {
            Log.e(TAG, "Error while loading party: " + e.getMessage());
            trackException(e);
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

}

package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.Session;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;

/**
 * Base activity for parties.
 */
public abstract class BasePartyActivity extends BaseActivity implements PartyListener {

    private static final String TAG = "BasePartyActivity";

    /**
     * Action for broadcast intent indicating that a party is updated.
     */
    public static final String ACTION_PARTY_UPDATE = "party_update";

    /**
     * Intent extra for the party ID.
     */
    public static final String EXTRA_PARTY_ID = "party_id";

    private static final int LOADER_PARTY = 1;
    private static final String LOADER_ARGS_PARTY_ID = "party_id";

    private long partyID;

    private BroadcastReceiver partyUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long updatedPartyID = intent.getLongExtra(EXTRA_PARTY_ID, 0);
            // Reload party if our party was updated
            if (updatedPartyID == getPartyID()) {
                loadParty();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_PARTY_ID)) {
            partyID = intent.getLongExtra(EXTRA_PARTY_ID, 0);
        } else {
            throw new IllegalArgumentException("Missing required party ID in intent");
        }
    }

    protected long getPartyID() {
        return partyID;
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        loadParty();
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        getSupportLoaderManager().destroyLoader(LOADER_PARTY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EXTRA_PARTY_ID, partyID);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register to receive party update broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(partyUpdateReceiver,
                new IntentFilter(ACTION_PARTY_UPDATE));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister from party update broadcasts
        LocalBroadcastManager.getInstance(this).unregisterReceiver(partyUpdateReceiver);
    }

    protected void loadParty() {
        Bundle args = new Bundle();
        args.putLong(LOADER_ARGS_PARTY_ID, getPartyID());
        getSupportLoaderManager().restartLoader(LOADER_PARTY, args, new PartyLoaderCallbacks());
    }

    /**
     * Redirects to the appropriate activity
     * based on the new party state.
     */
    protected void redirectIfNeeded(Party party, User user) {
        Class<?> targetActivity = null;
        if (party.isHost(user)) {
            // User is host
            switch (party.getStatus()) {
                case INVITING:
                    targetActivity = PartyInviteActivity.class;
                    break;
                case PLANNING:
                    targetActivity = PlanPartyActivity.class;
                    break;
                case PLANNED:
                    targetActivity = ViewPartyActivity.class;
                    break;
            }
        } else if (party.isInParty(user)) {
            // User is partner
            // TODO apart van wanneer host dit doet?
            targetActivity = ViewPartyActivity.class;
        } else {
            // User is invited to party
            targetActivity = InviteReplyActivity.class;
        }

        // Seriously Android Studio, get your sh*t together.
        // I should not need a cast for this.
        Class<?> ownClass = ((Object) this).getClass();
        if (targetActivity != null && !targetActivity.isAssignableFrom(ownClass)) {
            Intent intent = new Intent(this, targetActivity);
            intent.putExtra(EXTRA_PARTY_ID, party.getID());
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }
    }

    private void firePartyLoaded(Party party) {
        User user = getLoggedInUser();
        if (user == null) return;

        // Call own listener
        onPartyLoaded(party, user);

        // Call fragment listeners
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isAdded() && fragment instanceof PartyListener) {
                    ((PartyListener) fragment).onPartyLoaded(party, user);
                }
            }
        }
    }

    private void firePartyUnloaded() {
        // Call own listener
        onPartyUnloaded();

        // Call fragment listeners
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isAdded() && fragment instanceof PartyListener) {
                    ((PartyListener) fragment).onPartyUnloaded();
                }
            }
        }
    }

    @Override
    public void onPartyLoaded(Party party, User user) {
        redirectIfNeeded(party, user);
    }

    @Override
    public void onPartyUnloaded() {
    }

    private class PartyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Party> {

        @Override
        public Loader<Party> onCreateLoader(int id, Bundle args) {
            long partyID = args.getLong(LOADER_ARGS_PARTY_ID);
            return new PartyLoader(BasePartyActivity.this, partyID);
        }

        @Override
        public void onLoadFinished(Loader<Party> loader, Party party) {
            // Fire listeners
            if (party != null) {
                firePartyLoaded(party);
            } else {
                // TODO Error handling?
                firePartyUnloaded();
            }
        }

        @Override
        public void onLoaderReset(Loader<Party> loader) {
            // Fire listeners
            firePartyUnloaded();
        }

    }

}

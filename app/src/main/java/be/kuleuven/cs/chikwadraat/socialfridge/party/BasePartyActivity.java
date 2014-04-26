package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.Session;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyLoaderService;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;

/**
 * Base activity for parties.
 */
public abstract class BasePartyActivity extends BaseActivity implements PartyListener {

    /**
     * Intent extra for the party ID.
     */
    public static final String EXTRA_PARTY_ID = "party_id";
    private static final String TAG = "BasePartyActivity";

    private Party party;
    private long partyID;

    private BroadcastReceiver partyUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long updatedPartyID = intent.getLongExtra(PartyLoaderService.EXTRA_PARTY_ID, 0);
            // Load party if our party was updated
            if (updatedPartyID == getPartyID()) {
                Party party = intent.getParcelableExtra(PartyLoaderService.EXTRA_PARTY_OBJECT);
                firePartyLoaded(party);
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

    protected Party getParty() {
        return party;
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        loadParty();
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        firePartyUnloaded();
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
                new IntentFilter(PartyLoaderService.ACTION_PARTY_UPDATE));
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister from party update broadcasts
        LocalBroadcastManager.getInstance(this).unregisterReceiver(partyUpdateReceiver);
    }

    /**
     * Load the party.
     */
    protected void loadParty() {
        PartyLoaderService.startLoad(this, getPartyID());
    }

    /**
     * Reload the party.
     */
    protected void reloadParty() {
        PartyLoaderService.startReload(this, getPartyID());
    }

    /**
     * Cache the received party.
     */
    protected void cacheParty(Party party) {
        PartyLoaderService.cacheParty(party);
    }

    /**
     * Redirects to the appropriate activity
     * based on the new party state.
     */
    protected void redirectIfNeeded(Party party, User user) {
        if (isFinishing()) return;

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
        } else if (party.isInviting()) {
            // User is invited to party
            targetActivity = InviteReplyActivity.class;
        } else {
            // Unauthorized
            finish();
            return;
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
        this.party = party;

        redirectIfNeeded(party, user);
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onPartyUnloaded() {
        this.party = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (allowRemoveParty()) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.party_remove, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (allowRemoveParty()) {
            MenuItem removeItem = menu.findItem(R.id.party_action_remove);
            boolean removeEnabled = false;

            Party party = getParty();
            User user = getLoggedInUser();
            if (canRemoveParty(party, user)) {
                // Set text
                if (canDisbandParty(party, user)) {
                    removeItem.setTitle(getString(R.string.party_action_disband));
                    removeEnabled = true;
                } else if (canDeleteParty(party, user)) {
                    removeItem.setTitle(getString(R.string.party_action_delete));
                    removeEnabled = true;
                }
            }

            // Enable and show
            removeItem.setVisible(removeEnabled);
            removeItem.setEnabled(removeEnabled);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.party_action_remove:
                removeParty();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected boolean allowRemoveParty() {
        return true;
    }

    protected boolean canRemoveParty(Party party, User user) {
        return party != null && user != null && allowRemoveParty();
    }

    protected boolean canDisbandParty(Party party, User user) {
        return !canDeleteParty(party, user) && party.isHost(user);
    }

    protected boolean canDeleteParty(Party party, User user) {
        return party.isCompleted() || party.isDisbanded();
    }

    protected void removeParty() {
        Party party = getParty();
        User user = getLoggedInUser();
        if (!canRemoveParty(party, user)) return;

        if (canDisbandParty(party, user)) {
            disbandParty(party, user);
        } else if (canDeleteParty(party, user)) {
            deleteParty(party, user);
        } else {
            Log.i(TAG, "Illegal attempt to delete party");
        }
    }

    protected void disbandParty(Party party, User user) {
        // TODO
    }

    protected void deleteParty(Party party, User user) {
        // TODO
    }

}

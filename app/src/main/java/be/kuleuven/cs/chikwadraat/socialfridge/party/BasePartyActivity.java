package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.facebook.Session;
import com.google.android.gms.analytics.HitBuilders;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Joiner;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyLoaderService;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;

/**
 * Base activity for parties.
 */
public abstract class BasePartyActivity extends BaseActivity implements PartyListener {

    private static final String TAG = "BasePartyActivity";

    /**
     * Intent extra for the party ID.
     */
    public static final String EXTRA_PARTY_ID = "party_id";
    private static final String EXTRA_PARTY_OBJECT = "party_object";

    private long partyID;
    private Party party;

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

        if (savedInstanceState != null) {
            party = savedInstanceState.getParcelable(EXTRA_PARTY_OBJECT);
            if (party != null && party.getID() != partyID) {
                party = null;
            }
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
        outState.putParcelable(EXTRA_PARTY_OBJECT, party);
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
        this.party = party;
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
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void firePartyLoaded(Party party) {
        if (!isStarted()) return;

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
        if (!isStarted()) return;

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
            disbandParty(party);
        } else if (canDeleteParty(party, user)) {
            deleteParty(party);
        } else {
            Log.i(TAG, "Illegal attempt to delete party");
        }
    }

    protected void disbandParty(final Party party) {
        int nbPartners = party.getPartners().size() - 1;
        String invites = (party.isInviting())
                ? getString(R.string.party_dialog_confirm_disband_invites)
                : null;
        String notify = (nbPartners > 0)
                ? getResources().getQuantityString(R.plurals.party_dialog_confirm_disband_notify, nbPartners, nbPartners)
                : null;
        String message = Joiner.on('\n').skipNulls().join(invites, notify);

        new AlertDialog.Builder(this)
                .setTitle(R.string.party_dialog_confirm_disband_title)
                .setMessage(Strings.emptyToNull(message))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doDisbandParty(party);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    protected void deleteParty(final Party party) {
        doDeleteParty(party);
    }

    protected void doDisbandParty(Party party) {
        new RemovePartyTask(RemoveType.DISBAND, party.getID()).execute();
    }

    protected void doDeleteParty(Party party) {
        new RemovePartyTask(RemoveType.DELETE, party.getID()).execute();
    }

    private void onPartyDisbanded(Party party) {
        // Party disbanded
        Log.d(TAG, "Party successfully disbanded");
        hideProgressDialog();

        // Cache updated party
        cacheParty(party);

        // Done
        getTracker().send(new HitBuilders.EventBuilder("Party", "Disband").build());
        finish();
    }

    private void onPartyDeleted(long partyID) {
        // Party deleted
        Log.d(TAG, "Party successfully deleted");
        hideProgressDialog();

        // Done
        getTracker().send(new HitBuilders.EventBuilder("User", "DeleteParty").build());
        finish();
    }

    private void onPartyRemoveError(Exception exception) {
        Log.e(TAG, "Failed to remove party: " + exception.getMessage());
        hideProgressDialog();
        trackException(exception);

        // Handle regular exception
        handleException(exception);
    }

    private class RemovePartyTask extends AsyncTask<Void, Void, Party> {

        private final RemoveType type;
        private final long partyID;

        private Exception exception;

        private RemovePartyTask(RemoveType type, long partyID) {
            this.type = type;
            this.partyID = partyID;
        }

        @Override
        protected Party doInBackground(Void... params) {
            try {
                String accessToken = Session.getActiveSession().getAccessToken();
                switch (type) {
                    case DISBAND:
                        return new Party(Endpoints.parties().disband(partyID, accessToken).execute());
                    case DELETE:
                        Endpoints.users().removeParty(partyID, accessToken).execute();
                        return null;
                    default:
                        throw new IllegalArgumentException("Unknown remove type: " + type);
                }
            } catch (Exception e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Party party) {
            if (exception != null) {
                onPartyRemoveError(exception);
            } else {
                switch (type) {
                    case DISBAND:
                        onPartyDisbanded(party);
                        break;
                    case DELETE:
                        onPartyDeleted(partyID);
                        break;
                }
            }
        }

    }

    private enum RemoveType {
        DISBAND, DELETE
    }

}

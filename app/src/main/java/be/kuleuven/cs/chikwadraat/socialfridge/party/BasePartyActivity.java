package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.facebook.Session;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Base activity for parties.
 */
public abstract class BasePartyActivity extends BaseActivity implements PartyListener {

    private static final String TAG = "BasePartyActivity";

    private static final String EXTRA_PARTY_ID = "party_id";

    private static final int LOADER_PARTY = 1;
    private static final String LOADER_ARGS_PARTY_ID = "party_id";
    private static final String LOADER_ARGS_USER_ID = "user_id";

    private Long partyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_PARTY_ID)) {
            partyID = savedInstanceState.getLong(EXTRA_PARTY_ID);
        } else if (intent != null && intent.hasExtra(EXTRA_PARTY_ID)) {
            partyID = intent.getLongExtra(EXTRA_PARTY_ID, 0);
        } else {
            partyID = null;
        }
    }

    protected Long getPartyID() {
        return partyID;
    }

    protected void setPartyID(long partyID) {
        this.partyID = partyID;

        // Store in intent
        Intent intent = getIntent();
        intent.putExtra(EXTRA_PARTY_ID, partyID);
        setIntent(intent);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);

        loadParty(partyID, user.getId());
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

    protected void loadParty(Long partyID, String userID) {
        Bundle args = new Bundle();
        if (partyID != null) {
            args.putLong(LOADER_ARGS_PARTY_ID, partyID);
        }
        args.putString(LOADER_ARGS_USER_ID, userID);
        getSupportLoaderManager().restartLoader(LOADER_PARTY, args, new PartyLoaderCallbacks());
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
    }

    @Override
    public void onPartyUnloaded() {
    }

    private class PartyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Party> {

        @Override
        public Loader<Party> onCreateLoader(int id, Bundle args) {
            Long partyID = null;
            if (args.containsKey(LOADER_ARGS_PARTY_ID)) {
                partyID = args.getLong(LOADER_ARGS_PARTY_ID);
            }
            String userID = args.getString(LOADER_ARGS_USER_ID);
            return new PartyLoader(BasePartyActivity.this, partyID, userID);
        }

        @Override
        public void onLoadFinished(Loader<Party> loader, Party party) {
            PartyLoader partyLoader = (PartyLoader) loader;

            // Store party ID
            setPartyID(party.getId());

            // Fire listeners
            firePartyLoaded(party);
        }

        private List<PartyMember> getPartners(List<PartyMember> members) {
            List<PartyMember> partners = new ArrayList<PartyMember>(members.size());
            for (PartyMember member : members) {
                if (member.getInParty()) {
                    partners.add(member);
                }
            }
            return partners;
        }

        @Override
        public void onLoaderReset(Loader<Party> loader) {
            // Fire listeners
            firePartyUnloaded();
        }
    }

}

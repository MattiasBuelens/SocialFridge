package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;

import com.facebook.Session;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Party activity.
 */
public class PartyActivity extends BaseActivity implements CandidatesFragment.CandidateListener {

    private static final String TAG = "PartyActivity";

    private static final String EXTRA_PARTY_ID = "party_id";

    private static final int LOADER_PARTY = 1;
    private static final String LOADER_ARGS_PARTY_ID = "party_id";
    private static final String LOADER_ARGS_USER_ID = "user_id";

    private Long partyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party);

        Intent intent = getIntent();
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_PARTY_ID)) {
            partyID = savedInstanceState.getLong(EXTRA_PARTY_ID);
        } else if (intent != null && intent.hasExtra(EXTRA_PARTY_ID)) {
            partyID = intent.getLongExtra(EXTRA_PARTY_ID, 0);
        } else {
            partyID = null;
        }
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
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) return;

        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isAdded() && fragment instanceof PartyListener) {
                ((PartyListener) fragment).onPartyLoaded(party, getLoggedInUser());
            }
        }
    }

    private void firePartyUnloaded() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) return;

        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isAdded() && fragment instanceof PartyListener) {
                ((PartyListener) fragment).onPartyUnloaded();
            }
        }
    }

    @Override
    public void onCandidateInvited(PartyMember candidate) {
        new InviteTask(candidate).execute();
    }

    @Override
    public void onCandidateInviteCanceled(PartyMember candidate) {
        new CancelInviteTask(candidate).execute();
    }

    private Parties getEndpoint() {
        Parties.Builder builder = new Parties.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
        return Endpoints.prepare(builder, this).build();
    }

    private class PartyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Party> {

        @Override
        public Loader<Party> onCreateLoader(int id, Bundle args) {
            Long partyID = null;
            if (args.containsKey(LOADER_ARGS_PARTY_ID)) {
                partyID = args.getLong(LOADER_ARGS_PARTY_ID);
            }
            String userID = args.getString(LOADER_ARGS_USER_ID);
            return new PartyLoader(PartyActivity.this, partyID, userID);
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

    private class InviteTask extends AsyncTask<Void, Void, Boolean> {

        private final PartyMember candidate;

        private InviteTask(PartyMember candidate) {
            this.candidate = candidate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                getEndpoint().invite(candidate.getPartyID(), candidate.getUserID(), getSession().getAccessToken()).execute();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error while inviting: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                candidate.setInvited(true);
            }
        }
    }

    private class CancelInviteTask extends AsyncTask<Void, Void, Boolean> {

        private final PartyMember candidate;

        private CancelInviteTask(PartyMember candidate) {
            this.candidate = candidate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                getEndpoint().cancelInvite(candidate.getPartyID(), candidate.getUserID(), getSession().getAccessToken()).execute();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error while canceling invite: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                candidate.setInvited(false);
            }
        }
    }

}

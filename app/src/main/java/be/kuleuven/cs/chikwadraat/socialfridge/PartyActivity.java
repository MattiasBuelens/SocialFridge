package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyCandidatesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;

/**
 * Party activity.
 * <p/>
 * Invite partners
 */
public class PartyActivity extends ListActivity {

    private static final String TAG = "PartyActivity";

    private static final String EXTRA_PARTY_ID = "party_id";

    private static final int LOADER_PARTY = 1;
    private static final int LOADER_CANDIDATES = 2;
    private static final String LOADER_ARGS_PARTY_ID = "party_id";
    private static final String LOADER_ARGS_USER_ID = "user_id";

    private Long partyID;

    private PartnersListAdapter partnersAdapter;
    private CandidatesListAdapter candidatesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View header = getLayoutInflater().inflate(R.layout.party, null);
        getListView().addHeaderView(header);

        Intent intent = getIntent();
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_PARTY_ID)) {
            partyID = savedInstanceState.getLong(EXTRA_PARTY_ID);
        } else if (intent != null && intent.hasExtra(EXTRA_PARTY_ID)) {
            partyID = intent.getLongExtra(EXTRA_PARTY_ID, 0);
        } else {
            partyID = null;
        }

        GridView partnersGrid = (GridView) header.findViewById(R.id.party_partners_list);
        partnersAdapter = new PartnersListAdapter(this);
        partnersGrid.setAdapter(partnersAdapter);

        candidatesAdapter = new CandidatesListAdapter(this);
        setListAdapter(candidatesAdapter);
    }

    protected void setPartyID(long partyID) {
        this.partyID = partyID;

        // Store in intent
        Intent intent = getIntent();
        intent.putExtra(EXTRA_PARTY_ID, partyID);
        setIntent(intent);
    }

    @Override
    protected void onLoggedIn(final Session session) {
        requestUserID(session, new UserIDCallback() {
            @Override
            public void onSuccess(String userID) {
                loadParty(partyID, userID);
            }

            @Override
            public void onError(FacebookRequestError error) {
                handleError(error);
            }
        });
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        getSupportLoaderManager().destroyLoader(LOADER_PARTY);
        getSupportLoaderManager().destroyLoader(LOADER_CANDIDATES);
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

    protected void loadCandidates(long partyID) {
        Bundle args = new Bundle();
        args.putLong(LOADER_ARGS_PARTY_ID, partyID);
        getSupportLoaderManager().restartLoader(LOADER_CANDIDATES, args, new CandidatesLoaderCallbacks());
    }

    public static class PartnersListAdapter extends ArrayAdapter<PartyMember> {

        public PartnersListAdapter(Context context) {
            super(context, R.layout.partner_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.partner_list_item, null);
            }

            ProfilePictureView pictureView = (ProfilePictureView) view.findViewById(R.id.partner_pic);

            PartyMember partner = getItem(position);
            pictureView.setProfileId(partner.getUserID());

            return view;
        }
    }

    public static class CandidatesListAdapter extends ArrayAdapter<PartyMember> {

        public CandidatesListAdapter(Context context) {
            super(context, R.layout.candidate_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.candidate_list_item, null);
            }

            ProfilePictureView pictureView = (ProfilePictureView) view.findViewById(R.id.candidate_pic);
            TextView nameView = (TextView) view.findViewById(R.id.candidate_name);
            Button inviteButton = (Button) view.findViewById(R.id.candidate_invite);
            //Button cancelInviteButton = (Button) view.findViewById(R.id.candidate_cancel_invite);

            PartyMember candidate = getItem(position);
            pictureView.setProfileId(candidate.getUserID());
            nameView.setText(candidate.getUserName());

            // TODO Improve API for condition?
            if (candidate.getStatus().equals("INVITED")) {
                inviteButton.setText(R.string.party_partner_status_invited);
                inviteButton.setEnabled(false);
                //cancelInviteButton.setVisibility(View.VISIBLE);
            } else {
                inviteButton.setText(R.string.party_action_invite);
                inviteButton.setEnabled(true);
                //cancelInviteButton.setVisibility(View.INVISIBLE);
            }

            return view;
        }
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
            String userID = partyLoader.getUserID();

            // Store party ID
            setPartyID(party.getId());

            // TODO Filter on actual partners?
            partnersAdapter.setData(party.getMembers());

            // Load candidates if host
            if (userID.equals(party.getHostID())) {
                loadCandidates(party.getId());
            }
        }

        @Override
        public void onLoaderReset(Loader<Party> loader) {
            partnersAdapter.clear();
            getSupportLoaderManager().destroyLoader(LOADER_CANDIDATES);
        }
    }

    private class CandidatesLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<PartyMember>> {

        @Override
        public Loader<List<PartyMember>> onCreateLoader(int id, Bundle args) {
            long partyID = args.getLong(LOADER_ARGS_PARTY_ID);
            return new PartyCandidatesLoader(PartyActivity.this, partyID);
        }

        @Override
        public void onLoadFinished(Loader<List<PartyMember>> loader, List<PartyMember> candidates) {
            PartyCandidatesLoader candidatesLoader = (PartyCandidatesLoader) loader;
            long partyID = candidatesLoader.getPartyID();
            candidatesAdapter.setData(candidates);

            // TODO Bind invite / cancel invite buttons?
        }

        @Override
        public void onLoaderReset(Loader<List<PartyMember>> loader) {
            candidatesAdapter.clear();
        }
    }

}

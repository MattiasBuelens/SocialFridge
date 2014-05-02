package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;
import com.google.android.gms.analytics.HitBuilders;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.CandidatesFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.DetailsFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.parties;

/**
 * Activity to invite friends to a party.
 */
public class PartyInviteActivity extends BasePartyActivity implements CandidatesFragment.CandidateListener, ObservableAsyncTask.Listener<Void, Party>, View.OnClickListener {

    private static final String TAG = "PartyInviteActivity";

    private DetailsFragment detailsFragment;
    private CandidatesFragment candidatesFragment;
    private Button confirmPartnersButton;

    private PartyEndpointAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_invite);

        detailsFragment = (DetailsFragment) getSupportFragmentManager().findFragmentById(R.id.details_fragment);
        candidatesFragment = (CandidatesFragment) getSupportFragmentManager().findFragmentById(R.id.candidates_fragment);
        confirmPartnersButton = (Button) findViewById(R.id.party_action_confirm_partners);

        candidatesFragment.addHeaderView(detailsFragment.getView());
        confirmPartnersButton.setOnClickListener(this);

        // Re-attach to close invites task
        task = (PartyEndpointAsyncTask) getLastCustomNonConfigurationInstance();
        if (task != null) {
            task.attach(this);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (task != null) {
            task.detach();
        }
        return task;
    }

    @Override
    public void onCandidateInvited(PartyMember candidate) {
        new InviteTask(candidate).execute();
    }

    @Override
    public void onCandidateInviteCanceled(PartyMember candidate) {
        new CancelInviteTask(candidate).execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.party_action_confirm_partners:
                closeInvites();
                break;
        }
    }

    private void closeInvites() {
        if (task != null) return;

        try {
            task = new PartyEndpointAsyncTask(this, parties().closeInvites(
                    getPartyID(),
                    getSession().getAccessToken()));
            task.execute();
            showProgressDialog(R.string.party_close_invites_progress);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing close invites request: " + e.getMessage());
            trackException(e);
        }
    }

    private void removeCloseInvitesTask() {
        if (task != null) {
            task.detach();
            task = null;
        }
    }

    @Override
    public void onResult(Party party) {
        // Invites closed
        Log.d(TAG, "Party invites successfully closed");
        removeCloseInvitesTask();
        hideProgressDialog();

        // Cache updated party
        cacheParty(party);

        // Start planning
        getTracker().send(new HitBuilders.EventBuilder("Party", "Planning").build());
        Intent intent = new Intent(this, PlanPartyActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, getPartyID());

        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to close party invites: " + exception.getMessage());
        removeCloseInvitesTask();
        hideProgressDialog();
        trackException(exception);

        // Handle regular exception
        handleException(exception);
    }

    @Override
    public void onProgress(Void... progress) {

    }

    private class InviteTask extends AsyncTask<Void, Void, Exception> {

        private final PartyMember candidate;

        private InviteTask(PartyMember candidate) {
            this.candidate = candidate;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                parties().invite(candidate.getPartyID(), candidate.getUserID(), Session.getActiveSession().getAccessToken()).execute();
                return null;
            } catch (IOException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception exception) {
            if (exception == null) {
                candidate.setInvited(true);
                candidatesFragment.refreshCandidates();
            } else {
                Log.e(TAG, "Error while inviting: " + exception.getMessage());
                trackException(exception);
                // TODO Error handling?
            }
        }

        private Parties parties() {
            return Endpoints.parties();
        }

    }

    private class CancelInviteTask extends AsyncTask<Void, Void, Exception> {

        private final PartyMember candidate;

        private CancelInviteTask(PartyMember candidate) {
            this.candidate = candidate;
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                parties().cancelInvite(candidate.getPartyID(), candidate.getUserID(), Session.getActiveSession().getAccessToken()).execute();
                return null;
            } catch (IOException e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception exception) {
            if (exception == null) {
                candidate.setInvited(false);
                candidatesFragment.refreshCandidates();
            } else {
                Log.e(TAG, "Error while canceling invite: " + exception.getMessage());
                trackException(exception);
                // TODO Error handling?
            }
        }

        private Parties parties() {
            return Endpoints.parties();
        }

    }

}

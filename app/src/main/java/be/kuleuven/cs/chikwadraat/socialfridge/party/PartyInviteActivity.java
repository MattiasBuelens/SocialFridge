package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;
import com.google.android.gms.analytics.HitBuilders;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
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
        int nbPendingInvites = getParty().getInvitees().size();
        if (nbPendingInvites > 0) {
            String message = getResources().getQuantityString(R.plurals.party_dialog_confirm_close_inviting_withdraw,
                    nbPendingInvites, nbPendingInvites);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.party_dialog_confirm_close_inviting_title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doCloseInvites();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else {
            doCloseInvites();
        }

    }

    private void doCloseInvites() {
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

    private class InviteTask extends AsyncTask<Void, Void, Party> {

        private final PartyMember candidate;
        private Exception exception;

        private InviteTask(PartyMember candidate) {
            this.candidate = candidate;
        }

        @Override
        protected Party doInBackground(Void... params) {
            try {
                return new Party(parties().invite(
                        candidate.getPartyID(),
                        candidate.getUserID(),
                        Session.getActiveSession().getAccessToken()
                ).execute());
            } catch (IOException e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Party party) {
            if (party != null) {
                cacheParty(party);
                candidate.setInvited(true);
                candidatesFragment.refreshCandidates();
            } else if (exception != null) {
                Log.e(TAG, "Error while inviting: " + exception.getMessage());
                trackException(exception);
                // TODO Error handling?
            }
        }

    }

    private class CancelInviteTask extends AsyncTask<Void, Void, Party> {

        private final PartyMember candidate;
        private Exception exception;

        private CancelInviteTask(PartyMember candidate) {
            this.candidate = candidate;
        }

        @Override
        protected Party doInBackground(Void... params) {
            try {
                return new Party(parties().cancelInvite(
                        candidate.getPartyID(),
                        candidate.getUserID(),
                        Session.getActiveSession().getAccessToken()
                ).execute());
            } catch (IOException e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Party party) {
            if (party != null) {
                cacheParty(party);
                candidate.setInvited(false);
                candidatesFragment.refreshCandidates();
            } else if (exception != null) {
                Log.e(TAG, "Error while canceling invite: " + exception.getMessage());
                trackException(exception);
                // TODO Error handling?
            }
        }

    }

}

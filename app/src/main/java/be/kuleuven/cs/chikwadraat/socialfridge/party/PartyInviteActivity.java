package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Context;
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
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Activity to invite friends to a party.
 */
public class PartyInviteActivity extends BasePartyActivity implements CandidatesFragment.CandidateListener, ObservableAsyncTask.Listener<Void, Party>, View.OnClickListener {

    private static final String TAG = "PartyInviteActivity";

    private CandidatesFragment candidatesFragment;
    private Button doneButton;

    private CloseInvitesTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_invite);

        candidatesFragment = (CandidatesFragment) getSupportFragmentManager().findFragmentById(R.id.candidates_fragment);

        doneButton = (Button) findViewById(R.id.invite_action_done);
        doneButton.setOnClickListener(this);

        // Re-attach to close invites task
        task = (CloseInvitesTask) getLastCustomNonConfigurationInstance();
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
            case R.id.invite_action_done:
                closeInvites();
                break;
        }
    }

    private void closeInvites() {
        if (task != null) return;

        task = new CloseInvitesTask(this, getPartyID());
        task.execute();
        showProgressDialog(R.string.party_close_invites_progress);
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

        private final Context context;
        private final PartyMember candidate;

        private InviteTask(PartyMember candidate) {
            this.context = getApplicationContext();
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
            return Endpoints.parties(context);
        }

    }

    private class CancelInviteTask extends AsyncTask<Void, Void, Exception> {

        private final Context context;
        private final PartyMember candidate;

        private CancelInviteTask(PartyMember candidate) {
            this.context = getApplicationContext();
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
            return Endpoints.parties(context);
        }

    }

    private static class CloseInvitesTask extends ObservableAsyncTask<Void, Void, Party> {

        private final Context context;
        private final long partyID;

        private CloseInvitesTask(PartyInviteActivity activity, long partyID) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.partyID = partyID;
        }

        @Override
        protected Party run(Void... unused) throws Exception {
            return new Party(parties().closeInvites(partyID, Session.getActiveSession().getAccessToken()).execute());
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }

    }

}

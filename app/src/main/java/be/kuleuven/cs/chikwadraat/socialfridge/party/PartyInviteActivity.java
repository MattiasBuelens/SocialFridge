package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.CandidatesFragment;

/**
 * Activity to invite friends to a party.
 */
public class PartyInviteActivity extends BasePartyActivity implements CandidatesFragment.CandidateListener, ObservableAsyncTask.Listener<Void, Void>, View.OnClickListener {

    private static final String TAG = "PartyInviteActivity";

    private Button doneButton;

    private CloseInvitesTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_invite);

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
        new InviteTask(this, candidate).execute();
    }

    @Override
    public void onCandidateInviteCanceled(PartyMember candidate) {
        new CancelInviteTask(this, candidate).execute();
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
    public void onResult(Void aVoid) {
        Log.d(TAG, "Party invites successfully closed");
        removeCloseInvitesTask();
        hideProgressDialog();

        // Invites closed, start planning
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

        // Handle regular exception
        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.error_dialog_title)
                .setMessage(exception.getMessage())
                .show();
    }

    @Override
    public void onProgress(Void... progress) {

    }

    private static class InviteTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final PartyMember candidate;

        private InviteTask(Context context, PartyMember candidate) {
            this.context = context.getApplicationContext();
            this.candidate = candidate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                parties().invite(candidate.getPartyID(), candidate.getUserID(), Session.getActiveSession().getAccessToken()).execute();
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

        private Parties parties() {
            return Endpoints.parties(context);
        }

    }

    private static class CancelInviteTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final PartyMember candidate;

        private CancelInviteTask(Context context, PartyMember candidate) {
            this.context = context.getApplicationContext();
            this.candidate = candidate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                parties().cancelInvite(candidate.getPartyID(), candidate.getUserID(), Session.getActiveSession().getAccessToken()).execute();
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

        private Parties parties() {
            return Endpoints.parties(context);
        }

    }

    private static class CloseInvitesTask extends ObservableAsyncTask<Void, Void, Void> {

        private final Context context;
        private final long partyID;

        private CloseInvitesTask(PartyInviteActivity activity, long partyID) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.partyID = partyID;
        }

        @Override
        protected Void run(Void... unused) throws Exception {
            parties().closeInvites(partyID, Session.getActiveSession().getAccessToken()).execute();
            return null;
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }

    }

}

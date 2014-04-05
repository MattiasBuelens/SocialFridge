package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;

/**
 * Activity to invite friends to a party.
 */
public class PartyInviteActivity extends BasePartyActivity implements CandidatesFragment.CandidateListener {

    private static final String TAG = "PartyInviteActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party);
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

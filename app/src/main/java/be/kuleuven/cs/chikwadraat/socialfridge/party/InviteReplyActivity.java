package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.TimeSlotsFragment;

/**
 * Created by vital.dhaveloose on 29/03/2014.
 *
 * This Activity is displayed when users click the notification itself. It provides
 * UI for choosing time slots or as yet declining the invitation.
 */
public class InviteReplyActivity extends BasePartyActivity implements View.OnClickListener {

    private static final String TAG = "InviteReplyActivity";

    private Button joinButton;
    private JoinTask joinTask;
    private Button declineButton;
    private DeclineTask declineTask;
    private TimeSlotsFragment timeSlotsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_reply);

        // TODO: set up partners list

        // TODO: set up time slots fragments


        // set up join button
        joinButton = (Button) findViewById(R.id.invite_reply_action_join);
        joinButton.setOnClickListener(this);

        // set up decline button
        declineButton = (Button) findViewById(R.id.invite_reply_action_decline);
        declineButton.setOnClickListener(this);

    }

    public void onReady(View v) {
        //TODO: interpreteer geselecteerde slots, stuur bericht terug, sluit view af
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.invite_reply_action_join:
                join();
                break;
            case R.id.invite_reply_action_decline:
                decline();
                break;
        }
    }

    private void join() {
        if (joinTask != null) return;

        joinTask = new JoinTask(this, getPartyID());
        joinTask.execute();
        // showProgressDialog(R.string.party_close_invites_progress);
        // TODO: dialog tonen
    }

    private void decline() {
        if (declineTask != null) return;

        declineTask = new DeclineTask(this, getPartyID());
        declineTask.execute();
        // showProgressDialog(R.string.party_close_invites_progress);
        // TODO: dialog tonen
    }

    private static class JoinTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final PartyMember candidate;

        private JoinTask(Context context, PartyMember candidate) {
            this.context = context.getApplicationContext();
            this.candidate = candidate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: maak en verstuur bericht
            return null;
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }
    }

    private static class DeclineTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final PartyMember candidate;

        private DeclineTask(Context context, PartyMember candidate) {
            this.context = context.getApplicationContext();
            this.candidate = candidate;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: maak en verstuur bericht
            return null;
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }
    }
}

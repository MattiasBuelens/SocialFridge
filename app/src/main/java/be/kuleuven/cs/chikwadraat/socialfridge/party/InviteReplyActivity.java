package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.TimeSlotCollection;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.TimeSlotsFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

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

        // partners already set up by BasePartyActivity

        // TODO: set up time slots fragments
        timeSlotsFragment = (TimeSlotsFragment) getSupportFragmentManager().findFragmentById(R.id.time_slots_fragment);

        // set up join button

        joinButton = (Button) findViewById(R.id.invite_reply_action_join);
        joinButton.setOnClickListener(this);

        // set up decline button
        declineButton = (Button) findViewById(R.id.invite_reply_action_decline);
        declineButton.setOnClickListener(this);

    }

    @Override
    public void onPartyLoaded(Party party, User user) {
        super.onPartyLoaded(party, user);
        reconfigureTimeSlotsFragment(party.getTimeSlots());
    }

    private void reconfigureTimeSlotsFragment(Collection<TimeSlot> receivedSlots) {
        List<TimeSlotSelection> newSelections = new ArrayList<TimeSlotSelection>();
        for(TimeSlot slot : receivedSlots) {
            // TODO: assumptie dat onCreate hier al is aangeroepen...
            for(TimeSlotSelection selection : timeSlotsFragment.getTimeSlots()) {
                if(slot.getBeginHour().equals(selection.getBeginHour()) &&
                        slot.getEndHour().equals(selection.getEndHour())) {
                    newSelections.add(newSelection(selection, slot));
                }
            }
        }
    }

    private TimeSlotSelection newSelection(TimeSlotSelection currentSelection, TimeSlot receivedSlot) {
        TimeSlotSelection result = currentSelection;
        if(!receivedSlot.getAvailable()) {
            result.setState(TimeSlotSelection.State.DISABLED);
        }
        return result;
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

    private List<TimeSlot> getTimeSlots() {
        List<TimeSlot> result = new ArrayList<TimeSlot>();
        for(TimeSlotSelection selection : timeSlotsFragment.getTimeSlots()) {
            TimeSlot slot = new TimeSlot();
            slot.setBeginHour(selection.getBeginHour());
            slot.setEndHour(selection.getEndHour());
            slot.setAvailable(selection.isIncluded());
            result.add(slot);
        }
        return result;
    }

    private void join() {
        if (joinTask != null) return; // TODO: niet enkel joinTask testen, maar ook declineTask?
        TimeSlotCollection timeSlots = (new TimeSlotCollection()).setList(getTimeSlots());
        joinTask = new JoinTask(this, getPartyID(), timeSlots);
        joinTask.execute();
        // showProgressDialog(R.string.party_close_invites_progress);
        // TODO: dialog tonen
    }

    private void decline() {
        if (declineTask != null) return; // TODO: niet enkel joinTask testen, maar ook declineTask?

        declineTask = new DeclineTask(this, getPartyID());
        declineTask.execute();
        // showProgressDialog(R.string.party_close_invites_progress);
        // TODO: dialog tonen
    }

    private static class JoinTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final long partyID;
        private final TimeSlotCollection timeslots;

        private JoinTask(Context context, long partyID, TimeSlotCollection timeslots) {
            this.context = context;
            this.partyID = partyID;
            this.timeslots = timeslots;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                parties().acceptInvite(partyID, Session.getActiveSession().getAccessToken(), timeslots).execute();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error while replying to invite: " + e.getMessage());
                return false;
            }
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }
    }

    private static class DeclineTask extends AsyncTask<Void, Void, Boolean> {

        private final Context context;
        private final long partyID;

        private DeclineTask(Context context, long partyID) {
            this.context = context.getApplicationContext();
            this.partyID = partyID;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                parties().declineInvite(partyID, Session.getActiveSession().getAccessToken()).execute();
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error while replying to invite: " + e.getMessage());
                return false;
            }
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }
    }
}

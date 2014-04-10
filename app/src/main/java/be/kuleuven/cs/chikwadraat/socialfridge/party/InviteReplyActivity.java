package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;
import com.google.android.gms.analytics.HitBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlotCollection;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.TimeSlotsFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Created by vital.dhaveloose on 29/03/2014.
 * <p>
 * This Activity is displayed when users click the notification itself. It provides
 * UI for choosing time slots or as yet declining the invitation.
 * </p>
 */
public class InviteReplyActivity extends BasePartyActivity implements View.OnClickListener, ObservableAsyncTask.Listener<Void, Boolean> {

    private static final String TAG = "InviteReplyActivity";

    private Button joinButton;
    private Button declineButton;
    private TimeSlotsFragment timeSlotsFragment;

    private JoinDeclineTask task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invite_reply);

        timeSlotsFragment = (TimeSlotsFragment) getSupportFragmentManager().findFragmentById(R.id.time_slots_fragment);

        joinButton = (Button) findViewById(R.id.invite_reply_action_join);
        joinButton.setOnClickListener(this);

        declineButton = (Button) findViewById(R.id.invite_reply_action_decline);
        declineButton.setOnClickListener(this);

        // Re-attach to join/decline task
        task = (JoinDeclineTask) getLastCustomNonConfigurationInstance();
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
    public void onPartyLoaded(Party party, User user) {
        super.onPartyLoaded(party, user);
        reconfigureTimeSlotsFragment(party.getTimeSlots());
    }

    private void reconfigureTimeSlotsFragment(Collection<TimeSlot> receivedSlots) {
        List<TimeSlotSelection> newSelections = new ArrayList<TimeSlotSelection>();
        for (TimeSlot slot : receivedSlots) {
            TimeSlotSelection selection = timeSlotsFragment.getTimeSlot(slot.getBeginHour(), slot.getEndHour());
            if (selection == null) {
                // New time slot selection, probably on first load
                TimeSlotSelection.State state = slot.getAvailable()
                        ? TimeSlotSelection.State.INCLUDED
                        : TimeSlotSelection.State.DISABLED;
                selection = new TimeSlotSelection(slot.getBeginHour(), slot.getEndHour(), state);
            } else {
                // Existing selection, disable if no longer available
                if (!slot.getAvailable()) {
                    selection.setState(TimeSlotSelection.State.DISABLED);
                }
            }
            newSelections.add(selection);
        }
        timeSlotsFragment.setTimeSlots(newSelections);
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
        for (TimeSlotSelection selection : timeSlotsFragment.getTimeSlots()) {
            TimeSlot slot = new TimeSlot();
            slot.setBeginHour(selection.getBeginHour());
            slot.setEndHour(selection.getEndHour());
            slot.setAvailable(selection.isIncluded());
            result.add(slot);
        }
        return result;
    }

    private void join() {
        if (task != null) return;

        // TODO Validate that at least one slot is checked

        task = new JoinTask(this, getPartyID(), getTimeSlots());
        task.execute();

        showProgressDialog(R.string.party_join_progress);
    }

    private void decline() {
        if (task != null) return;

        task = new DeclineTask(this, getPartyID());
        task.execute();

        showProgressDialog(R.string.party_decline_progress);
    }

    private void removeTask() {
        if (task != null) {
            task.detach();
            task = null;
        }
    }

    @Override
    public void onResult(Boolean isJoined) {
        removeTask();
        hideProgressDialog();

        if (isJoined) {
            onJoined();
        } else {
            onDeclined();
        }
    }

    private void onJoined() {
        //Log.d(TAG, "Party successfully joined");
        getTracker().send(new HitBuilders.EventBuilder("PartyInvite", "Accept").build());

        // Joined, show party
        Intent intent = new Intent(this, ViewPartyActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, getPartyID());

        startActivity(intent);
        finish();
    }

    private void onDeclined() {
        //Log.d(TAG, "Invite successfully declined");
        getTracker().send(new HitBuilders.EventBuilder("PartyInvite", "Decline").build());

        // Declined invite, close
        finish();
    }

    @Override
    public void onError(Exception exception) {
        //Log.e(TAG, "Failed to reply to invite: " + exception.getMessage());
        trackException(exception);
        removeTask();
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

    private static abstract class JoinDeclineTask extends ObservableAsyncTask<Void, Void, Boolean> {

        protected final Context context;
        protected final long partyID;

        private JoinDeclineTask(InviteReplyActivity activity, long partyID) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.partyID = partyID;
        }

        protected Parties parties() {
            return Endpoints.parties(context);
        }

    }

    private static class JoinTask extends JoinDeclineTask {

        private final List<TimeSlot> timeSlots;

        private JoinTask(InviteReplyActivity activity, long partyID, List<TimeSlot> timeSlots) {
            super(activity, partyID);
            this.timeSlots = timeSlots;
        }

        @Override
        protected Boolean run(Void... unused) throws IOException {
            TimeSlotCollection timeSlotCollection = new TimeSlotCollection().setList(timeSlots);
            parties().acceptInvite(partyID, Session.getActiveSession().getAccessToken(), timeSlotCollection).execute();
            return true;
        }

    }

    private static class DeclineTask extends JoinDeclineTask {

        private DeclineTask(InviteReplyActivity activity, long partyID) {
            super(activity, partyID);
        }

        @Override
        protected Boolean run(Void... unused) throws IOException {
            parties().declineInvite(partyID, Session.getActiveSession().getAccessToken()).execute();
            return false;
        }

    }

}

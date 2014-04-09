package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.TimeSlotCollection;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.TimeSlotsFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;
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
            for (TimeSlotSelection selection : timeSlotsFragment.getTimeSlots()) {
                if (slot.getBeginHour().equals(selection.getBeginHour()) &&
                        slot.getEndHour().equals(selection.getEndHour())) {
                    newSelections.add(newSelection(selection, slot));
                }
            }
        }
        timeSlotsFragment.setTimeSlots(newSelections);
    }

    private TimeSlotSelection newSelection(TimeSlotSelection currentSelection, TimeSlot receivedSlot) {
        TimeSlotSelection result = currentSelection;
        if (!receivedSlot.getAvailable()) {
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
        // showProgressDialog(R.string.party_close_invites_progress);
        // TODO: dialog tonen
    }

    private void decline() {
        if (task != null) return;

        task = new DeclineTask(this, getPartyID());
        task.execute();
        // showProgressDialog(R.string.party_close_invites_progress);
        // TODO: dialog tonen
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
        Log.d(TAG, "Party successfully joined");

        // Joined, show party
        // TODO View party
    }

    private void onDeclined() {
        Log.d(TAG, "Invite successfully declined");

        // Declined invite, close
        finish();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to reply to invite: " + exception.getMessage());
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

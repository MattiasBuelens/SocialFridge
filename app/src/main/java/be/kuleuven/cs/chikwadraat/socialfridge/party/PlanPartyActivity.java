package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;
import com.google.android.gms.analytics.HitBuilders;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.TimeSlotPickerFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Activity to plan a party.
 */
public class PlanPartyActivity extends BasePartyActivity implements ObservableAsyncTask.Listener<Void, Void>, View.OnClickListener {

    private static final String TAG = "PlanPartyActivity";

    private TimeSlotPickerFragment timeSlotPicker;
    private Button doneButton;

    private PlanTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plan_party);

        timeSlotPicker = (TimeSlotPickerFragment) getSupportFragmentManager().findFragmentById(R.id.plan_time_slot_fragment);
        doneButton = (Button) findViewById(R.id.plan_action_done);
        doneButton.setOnClickListener(this);

        // Re-attach to plan task
        task = (PlanTask) getLastCustomNonConfigurationInstance();
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

        timeSlotPicker.setTimeSlots(toSelections(party.getTimeSlots()));
    }

    private TimeSlotSelection toSelection(TimeSlot slot) {
        TimeSlotSelection.State state = slot.isAvailable()
                ? TimeSlotSelection.State.INCLUDED
                : TimeSlotSelection.State.DISABLED;
        return new TimeSlotSelection(slot.getBeginHour(), slot.getEndHour(), state);
    }

    private List<TimeSlotSelection> toSelections(List<TimeSlot> slots) {
        List<TimeSlotSelection> selections = new ArrayList<TimeSlotSelection>(slots.size());
        for (TimeSlot slot : slots) {
            selections.add(toSelection(slot));
        }
        return selections;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plan_action_done:
                planParty();
                break;
        }
    }

    private void planParty() {
        if (task != null) return;

        TimeSlot slot = getPickedTimeSlot();
        if (slot == null) {
            // TODO No slot selected, show error?
            return;
        }

        task = new PlanTask(this, getPartyID(), slot);
        task.execute();
        showProgressDialog(R.string.party_plan_progress);
    }

    private void removePlanTask() {
        if (task != null) {
            task.detach();
            task = null;
        }
    }

    private TimeSlot getPickedTimeSlot() {
        TimeSlotSelection selection = timeSlotPicker.getPickedTimeSlot();
        return selection == null ? null : selection.toTimeSlot();
    }

    @Override
    public void onResult(Void unused) {
        // Party planned
        //Log.d(TAG, "Party successfully planned");
        removePlanTask();
        hideProgressDialog();

        // Reload party
        reloadParty();

        // Done
        getTracker().send(new HitBuilders.EventBuilder("Party", "Plan").build());
        Intent intent = new Intent(this, ViewPartyActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, getPartyID());

        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Exception exception) {
        //Log.e(TAG, "Failed to plan party: " + exception.getMessage());
        removePlanTask();
        hideProgressDialog();
        trackException(TAG, exception);

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

    private static class PlanTask extends ObservableAsyncTask<Void, Void, Void> {

        private final Context context;
        private final long partyID;
        private final TimeSlot timeSlot;

        private PlanTask(PlanPartyActivity activity, long partyID, TimeSlot timeSlot) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.partyID = partyID;
            this.timeSlot = timeSlot;
        }

        @Override
        protected Void run(Void... unused) throws Exception {
            parties().plan(partyID, Session.getActiveSession().getAccessToken(), timeSlot.toEndpoint()).execute();
            return null;
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }

    }

}

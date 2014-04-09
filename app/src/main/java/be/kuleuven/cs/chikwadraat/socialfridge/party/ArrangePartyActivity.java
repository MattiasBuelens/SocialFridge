package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.facebook.Session;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.ObservableAsyncTask;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Activity to arrange a party.
 */
public class ArrangePartyActivity extends BasePartyActivity implements ObservableAsyncTask.Listener<Void, Void>, View.OnClickListener {

    private static final String TAG = "ArrangePartyActivity";

    private TimeSlotPickerFragment timeSlotPicker;
    private Button doneButton;

    private ArrangeTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.arrange_party);

        timeSlotPicker = (TimeSlotPickerFragment) getSupportFragmentManager().findFragmentById(R.id.arrange_time_slot_fragment);
        doneButton = (Button) findViewById(R.id.arrange_action_done);
        doneButton.setOnClickListener(this);

        // Re-attach to arrange task
        task = (ArrangeTask) getLastCustomNonConfigurationInstance();
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
        TimeSlotSelection.State state = slot.getAvailable()
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
            case R.id.arrange_action_done:
                arrangeParty();
                break;
        }
    }

    private void arrangeParty() {
        if (task != null) return;

        TimeSlot slot = getPickedTimeSlot();
        if (slot == null) {
            // TODO No slot selected, show error?
            return;
        }

        task = new ArrangeTask(this, getPartyID(), slot);
        task.execute();
        showProgressDialog(R.string.party_arrange_progress);
    }

    private void removeArrangeTask() {
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
        Log.d(TAG, "Party successfully arranged");
        removeArrangeTask();
        hideProgressDialog();

        // Party arranged, done
        // TODO Replace with correct activity class
        Intent intent = new Intent(this, BasePartyActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, getPartyID());

        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to arrange party: " + exception.getMessage());
        removeArrangeTask();
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

    private static class ArrangeTask extends ObservableAsyncTask<Void, Void, Void> {

        private final Context context;
        private final long partyID;
        private final TimeSlot timeSlot;

        private ArrangeTask(ArrangePartyActivity activity, long partyID, TimeSlot timeSlot) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.partyID = partyID;
            this.timeSlot = timeSlot;
        }

        @Override
        protected Void run(Void... unused) throws Exception {
            parties().arrange(partyID, Session.getActiveSession().getAccessToken(), timeSlot).execute();
            return null;
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }

    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.facebook.Session;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyBuilder;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.party.BasePartyActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartyInviteActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.widget.ProgressDialogFragment;

/**
 * Time slot activity.
 * Choose time slots.
 */
public class TimeSlotActivity extends BaseActivity implements View.OnClickListener, ObservableAsyncTask.Listener<Void, Party> {

    private static final String TAG = "TimeSlotActivity";

    private static final String DayStateKey = "DayState";

    private TimeSlotsFragment timeSlotsFragment;
    private CreatePartyTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_timeslots);

        Button findPartnersButton = (Button) findViewById(R.id.time_action_find_partners);
        findPartnersButton.setOnClickListener(this);

        List<TimeSlotSelection> slots = new ArrayList<TimeSlotSelection>();
        slots.add(new TimeSlotSelection(17, 18, TimeSlotToggleState.UNSPECIFIED));
        slots.add(new TimeSlotSelection(18, 19, TimeSlotToggleState.INCLUDED));
        slots.add(new TimeSlotSelection(19, 20, TimeSlotToggleState.INCLUDED));
        slots.add(new TimeSlotSelection(20, 21, TimeSlotToggleState.EXCLUDED));

        timeSlotsFragment = (TimeSlotsFragment) getSupportFragmentManager().findFragmentById(R.id.time_slots_fragment);
        timeSlotsFragment.setDefaultTimeSlots(slots);

        // Re-attach to registration task
        task = (CreatePartyTask) getLastCustomNonConfigurationInstance();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time_action_find_partners:
                createParty();
                break;
        }
    }

    private Date getPartyDate() {
        RadioGroup dayGroup = (RadioGroup) findViewById(R.id.time_day_options);
        int checkedDayId = dayGroup.getCheckedRadioButtonId();
        Calendar calendar = Calendar.getInstance();
        if (checkedDayId == R.id.time_action_choose_tomorrow) {
            // Tomorrow
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTime();
    }

    private List<TimeSlot> getTimeSlots() {
        List<TimeSlotSelection> selections = timeSlotsFragment.getTimeSlots();
        List<TimeSlot> slots = new ArrayList<TimeSlot>();
        for (TimeSlotSelection selection : selections) {
            TimeSlot slot = new TimeSlot();
            slot.setBeginHour(selection.getBeginHour());
            slot.setEndHour(selection.getEndHour());
            slot.setAvailable(selection.isIncluded());
            slots.add(slot);
        }
        return slots;
    }

    private void createParty() {
        if (task != null) return;

        PartyBuilder builder = new PartyBuilder();
        builder.setHostID(getLoggedInUser().getId());
        builder.setDate(new DateTime(getPartyDate()));
        builder.setHostTimeSlots(getTimeSlots());

        task = new CreatePartyTask(this, builder);
        task.execute();
        showProgressDialog();
    }

    private void removeCreateTask() {
        if (task != null) {
            task.detach();
            task = null;
        }
    }

    @Override
    public void onResult(Party party) {
        Log.d(TAG, "Party successfully created");
        removeCreateTask();
        hideProgressDialog();

        // Party created, start inviting
        Intent intent = new Intent(this, PartyInviteActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, party.getId());

        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to create party: " + exception.getMessage());
        removeCreateTask();
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

    private void showProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment current = getSupportFragmentManager().findFragmentByTag("dialog");
        ProgressDialogFragment fragment;
        if (current != null) {
            fragment = (ProgressDialogFragment) current;
        } else {
            String progressMessage = getString(R.string.party_create_progress);
            fragment = ProgressDialogFragment.newInstance(progressMessage);
            fragment.setCancelable(false);
            ft.add(fragment, "dialog");
            ft.addToBackStack(null);
        }
        ft.show(fragment);
        ft.commit();
    }

    private void hideProgressDialog() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            ((ProgressDialogFragment) fragment).dismiss();
        }
    }

    protected static class CreatePartyTask extends ObservableAsyncTask<Void, Void, Party> {

        private final Context context;
        private final PartyBuilder builder;

        protected CreatePartyTask(TimeSlotActivity activity, PartyBuilder builder) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.builder = builder;
        }

        protected void attach(TimeSlotActivity activity) {
            super.attach(activity);
        }

        @Override
        protected Party run(Void... unused) throws Exception {
            return parties().insertParty(Session.getActiveSession().getAccessToken(), builder).execute();
        }

        private Parties parties() {
            return Endpoints.parties(context);
        }

    }

}



package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.google.android.gms.analytics.HitBuilders;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyBuilder;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.TimeSlotsFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.parties;

/**
 * Create party activity.
 */
public class CreatePartyActivity extends BaseActivity implements ObservableAsyncTask.Listener<Void, Party>, RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "CreatePartyActivity";

    private RadioGroup dayGroup;
    private TimeSlotsFragment timeSlotsFragment;
    private Button findPartnersButton;
    private PartyEndpointAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_create);

        dayGroup = (RadioGroup) findViewById(R.id.party_create_day_options);
        timeSlotsFragment = (TimeSlotsFragment) getSupportFragmentManager().findFragmentById(R.id.time_slots_fragment);
        findPartnersButton = (Button) findViewById(R.id.party_action_find_partners);

        findPartnersButton.setOnClickListener(this);
        updateTimeSlotSelections();

        // Re-attach to registration task
        task = (PartyEndpointAsyncTask) getLastCustomNonConfigurationInstance();
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
            case R.id.party_action_find_partners:
                createParty();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dayGroup.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dayGroup.setOnCheckedChangeListener(null);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group == dayGroup) {
            updateTimeSlotSelections();
        }
    }

    private Date getPartyDate() {
        int checkedDayId = dayGroup.getCheckedRadioButtonId();
        Calendar calendar = Calendar.getInstance();
        if (checkedDayId == R.id.party_create_choose_tomorrow) {
            // Tomorrow
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        // Clear the time
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private List<TimeSlotSelection> getTimeSlotSelections() {
        return timeSlotsFragment.getTimeSlots();
    }

    private void updateTimeSlotSelections() {
        // Create new selections
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getPartyDate());
        List<TimeSlotSelection> slots = new ArrayList<TimeSlotSelection>();
        // TODO Externalize begin and end hours
        for (int hour = 17; hour <= 20; hour++) {
            // Set begin and end dates
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            Date beginDate = calendar.getTime();
            calendar.set(Calendar.HOUR_OF_DAY, hour + 1);
            Date endDate = calendar.getTime();
            TimeSlotSelection.State state = TimeSlotSelection.State.INCLUDED;

            // Use state of old selection with same dates
            TimeSlotSelection oldSelection = timeSlotsFragment.getTimeSlot(beginDate, endDate);
            if (oldSelection != null) {
                state = oldSelection.getState();
            }

            // Add new selection
            slots.add(new TimeSlotSelection(beginDate, endDate, state));
        }

        timeSlotsFragment.setTimeSlots(slots);
    }

    private List<TimeSlot> getTimeSlots() {
        List<TimeSlotSelection> selections = timeSlotsFragment.getTimeSlots();
        List<TimeSlot> slots = new ArrayList<TimeSlot>();
        for (TimeSlotSelection selection : selections) {
            slots.add(selection.toTimeSlot());
        }
        return slots;
    }

    private void createParty() {
        if (task != null) return;

        PartyBuilder builder = new PartyBuilder();
        builder.setHostID(getLoggedInUser().getId());
        // TODO Dish
        builder.setDishID(null);
        builder.setDate(new DateTime(getPartyDate()));
        builder.setHostTimeSlots(TimeSlot.toEndpoint(getTimeSlots()));

        try {
            task = new PartyEndpointAsyncTask(this, parties()
                    .insertParty(getSession().getAccessToken(), builder));
            task.execute();
            showProgressDialog(R.string.party_create_progress);
        } catch (IOException e) {
            Log.e(TAG, "Error initializing party create request: " + e.getMessage());
            trackException(e);
        }
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
        getTracker().send(new HitBuilders.EventBuilder("Party", "Create").build());
        Intent intent = new Intent(this, PartyInviteActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, party.getID());

        startActivity(intent);
        finish();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to create party: " + exception.getMessage());
        removeCreateTask();
        hideProgressDialog();
        trackException(exception);

        // Handle regular exception
        handleException(exception);
    }

    @Override
    public void onProgress(Void... progress) {
    }

}



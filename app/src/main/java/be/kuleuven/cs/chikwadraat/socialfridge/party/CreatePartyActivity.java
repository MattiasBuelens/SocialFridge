package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyBuilder;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.party.fragments.TimeSlotsFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Create party activity.
 */
public class CreatePartyActivity extends BaseActivity implements View.OnClickListener, ObservableAsyncTask.Listener<Void, Party> {

    private static final String TAG = "CreatePartyActivity";

    private Button findPartnersButton;
    private RadioGroup dayGroup;
    private TimeSlotsFragment timeSlotsFragment;
    private CreatePartyTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_create);

        findPartnersButton = (Button) findViewById(R.id.party_action_find_partners);
        dayGroup = (RadioGroup) findViewById(R.id.party_create_day_options);
        timeSlotsFragment = (TimeSlotsFragment) getSupportFragmentManager().findFragmentById(R.id.time_slots_fragment);

        findPartnersButton.setOnClickListener(this);

        List<TimeSlotSelection> slots = new ArrayList<TimeSlotSelection>();
        slots.add(new TimeSlotSelection(17, 18, TimeSlotSelection.State.UNSPECIFIED));
        slots.add(new TimeSlotSelection(18, 19, TimeSlotSelection.State.INCLUDED));
        slots.add(new TimeSlotSelection(19, 20, TimeSlotSelection.State.EXCLUDED));
        slots.add(new TimeSlotSelection(20, 21, TimeSlotSelection.State.DISABLED));
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
            case R.id.party_action_find_partners:
                createParty();
                break;
        }
    }

    private Date getPartyDate() {
        int checkedDayId = dayGroup.getCheckedRadioButtonId();
        Calendar calendar = Calendar.getInstance();
        if (checkedDayId == R.id.party_create_choose_tomorrow) {
            // Tomorrow
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTime();
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
        builder.setDate(new DateTime(getPartyDate()));
        builder.setHostTimeSlots(getTimeSlots());

        task = new CreatePartyTask(this, builder);
        task.execute();
        showProgressDialog(R.string.party_create_progress);
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

    protected static class CreatePartyTask extends ObservableAsyncTask<Void, Void, Party> {

        private final Context context;
        private final PartyBuilder builder;

        protected CreatePartyTask(CreatePartyActivity activity, PartyBuilder builder) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.builder = builder;
        }

        protected void attach(CreatePartyActivity activity) {
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



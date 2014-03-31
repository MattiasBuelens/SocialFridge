package be.kuleuven.cs.chikwadraat.socialfridge;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Time slot activity.
 * Choose time slots.
 */
public class TimeSlotActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_timeslots);

        List<TimeSlotSelection> slots = new ArrayList<TimeSlotSelection>();
        slots.add(new TimeSlotSelection(17, 18, TimeSlotToggleState.UNSPECIFIED));
        slots.add(new TimeSlotSelection(18, 19, TimeSlotToggleState.INCLUDED));
        slots.add(new TimeSlotSelection(19, 20, TimeSlotToggleState.INCLUDED));
        slots.add(new TimeSlotSelection(20, 21, TimeSlotToggleState.EXCLUDED));

        TimeSlotsFragment fragment = (TimeSlotsFragment) getSupportFragmentManager().findFragmentById(R.id.time_slots_fragment);
        fragment.setDefaultTimeSlots(slots);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time_action_find_partners:


        }
    }

}



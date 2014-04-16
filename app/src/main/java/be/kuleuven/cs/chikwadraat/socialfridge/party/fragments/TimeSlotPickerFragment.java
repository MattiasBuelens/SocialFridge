package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ArrayAdapter;

/**
 * A fragment to pick one time slot.
 */
public class TimeSlotPickerFragment extends Fragment {

    private static final String ARG_PICKED_TIME_SLOT = "picked_time_slot";
    private static final String ARG_TIME_SLOTS = "time_slots";

    private TimeSlotSelection pickedTimeSlot;
    private ArrayList<TimeSlotSelection> timeSlots = new ArrayList<TimeSlotSelection>();

    private GridView timeSlotGrid;
    private TimeSlotPickerArrayAdapter timeSlotAdapter;

    /**
     * Create a new time slot picker fragment.
     *
     * @param timeSlots The time slots to choose from.
     * @return A new instance of fragment TimeSlotPickerFragment.
     */
    public static TimeSlotPickerFragment newInstance(List<TimeSlotSelection> timeSlots) {
        TimeSlotPickerFragment fragment = new TimeSlotPickerFragment();
        fragment.setTimeSlots(timeSlots);
        return fragment;
    }

    public TimeSlotPickerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pick_timeslot, container, false);

        timeSlotGrid = (GridView) view.findViewById(R.id.time_slot_grid);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore
            pickedTimeSlot = savedInstanceState.getParcelable(ARG_PICKED_TIME_SLOT);
            timeSlots = savedInstanceState.getParcelableArrayList(ARG_TIME_SLOTS);
        }

        timeSlotAdapter = new TimeSlotPickerArrayAdapter(getActivity());
        timeSlotGrid.setAdapter(timeSlotAdapter);
        setTimeSlots(timeSlots);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_PICKED_TIME_SLOT, getPickedTimeSlot());
        outState.putParcelableArrayList(ARG_TIME_SLOTS, timeSlots);
    }

    public TimeSlotSelection getPickedTimeSlot() {
        if (timeSlotAdapter != null) {
            TimeSlotSelection checked = timeSlotAdapter.getCheckedItem();
            if (checked != null) {
                return checked;
            }
        }
        return pickedTimeSlot;
    }

    protected TimeSlotSelection getTimeSlot(int beginHour, int endHour) {
        for (TimeSlotSelection slot : getTimeSlots()) {
            if (slot.getBeginHour() == beginHour && slot.getEndHour() == endHour) {
                return slot;
            }
        }
        return null;
    }

    protected void setPickedTimeSlot(TimeSlotSelection timeSlot) {
        if (timeSlot != null) {
            // Find own time slot
            timeSlot = getTimeSlot(timeSlot.getBeginHour(), timeSlot.getEndHour());
            // Reject unavailable time slots
            if (!timeSlot.isIncluded()) {
                timeSlot = null;
            }
        }
        this.pickedTimeSlot = timeSlot;
        if (timeSlotAdapter != null) {
            timeSlotAdapter.setCheckedItem(timeSlot);
        }
    }

    public List<TimeSlotSelection> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlotSelection> newTimeSlots) {
        TimeSlotSelection picked = getPickedTimeSlot();

        if (timeSlots == null) {
            timeSlots = new ArrayList<TimeSlotSelection>();
        } else {
            timeSlots = new ArrayList<TimeSlotSelection>(newTimeSlots);
        }
        if (timeSlotAdapter != null) {
            timeSlotAdapter.setData(timeSlots);
        }

        setPickedTimeSlot(picked);
    }

    public static class TimeSlotPickerArrayAdapter extends ArrayAdapter<TimeSlotSelection> implements CompoundButton.OnCheckedChangeListener {

        private int checkedPosition = -1;

        public TimeSlotPickerArrayAdapter(Context context) {
            super(context, R.layout.time_slot_check);
        }

        public int getCheckedPosition() {
            return checkedPosition;
        }

        public void setCheckedPosition(int checkedPosition) {
            this.checkedPosition = checkedPosition;
            notifyDataSetChanged();
        }

        public TimeSlotSelection getCheckedItem() {
            int position = getCheckedPosition();
            if (position >= 0 && position < getCount()) {
                return getItem(position);
            } else {
                return null;
            }
        }

        public void setCheckedItem(TimeSlotSelection item) {
            setCheckedPosition(getPosition(item));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder vh;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.time_slot_pick, null);
                vh = new ViewHolder();
                vh.radioButton = (RadioButton) view.findViewById(R.id.time_slot_pick);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }

            TimeSlotSelection slot = getItem(position);
            boolean isChecked = (getCheckedPosition() == position);
            vh.position = position;
            String text = slot.getBeginHour() + "h - " + slot.getEndHour() + "h";
            vh.radioButton.setText(text);
            vh.radioButton.setChecked(isChecked);
            vh.radioButton.setEnabled(slot.isIncluded());
            vh.radioButton.setOnCheckedChangeListener(this);
            vh.radioButton.setTag(vh);

            return view;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ViewHolder vh = (ViewHolder) buttonView.getTag();
            TimeSlotSelection slot = getItem(vh.position);
            if (!slot.isIncluded()) return;
            if (isChecked) {
                setCheckedItem(slot);
            }
        }

        private static class ViewHolder {
            RadioButton radioButton;
            int position;
        }

    }

}

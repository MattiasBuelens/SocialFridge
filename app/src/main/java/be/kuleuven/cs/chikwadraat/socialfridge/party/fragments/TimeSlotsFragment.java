package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotSelection;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * A fragment to configure time slots.
 */
public class TimeSlotsFragment extends Fragment {
    private static final String ARG_TIME_SLOTS = "time_slots";

    private ArrayList<TimeSlotSelection> timeSlotSelections = new ArrayList<TimeSlotSelection>();

    private GridView timeSlotGrid;
    private TimeSlotSelectionArrayAdapter timeSlotAdapter;

    public TimeSlotsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore
            timeSlotSelections = savedInstanceState.getParcelableArrayList(ARG_TIME_SLOTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_timeslots, container, false);

        timeSlotGrid = (GridView) view.findViewById(R.id.time_slot_grid);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        timeSlotAdapter = new TimeSlotSelectionArrayAdapter(getActivity(), timeSlotSelections);
        timeSlotGrid.setAdapter(timeSlotAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(ARG_TIME_SLOTS, timeSlotSelections);
    }

    public List<TimeSlotSelection> getTimeSlots() {
        return timeSlotSelections;
    }

    public TimeSlotSelection getTimeSlot(Date beginDate, Date endDate) {
        for (TimeSlotSelection selection : getTimeSlots()) {
            if (selection.getBeginDate().equals(beginDate) &&
                    selection.getEndDate().equals(endDate)) {
                return selection;
            }
        }
        return null;
    }

    public void setTimeSlots(List<TimeSlotSelection> timeSlots) {
        if (timeSlotAdapter != null) {
            AdapterUtils.setAll(timeSlotAdapter, timeSlots);
        } else {
            timeSlotSelections = new ArrayList<TimeSlotSelection>(timeSlots);
        }
    }

    public static class TimeSlotSelectionArrayAdapter extends ArrayAdapter<TimeSlotSelection> implements CompoundButton.OnCheckedChangeListener {

        public TimeSlotSelectionArrayAdapter(Context context, List<TimeSlotSelection> selections) {
            super(context, R.layout.time_slot_check, selections);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder vh;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.time_slot_check, null);
                vh = new ViewHolder();
                vh.toggleButton = (ToggleButton) view.findViewById(R.id.time_slot_toggle);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }

            TimeSlotSelection slot = getItem(position);
            vh.position = position;
            String beginDateText = DateFormat.getTimeFormat(getContext()).format(slot.getBeginDate());
            String endDateText = DateFormat.getTimeFormat(getContext()).format(slot.getEndDate());
            String text = getContext().getString(R.string.party_time_slot_format, beginDateText, endDateText);
            vh.toggleButton.setTextOff(text);
            vh.toggleButton.setTextOn(text);
            vh.toggleButton.setChecked(slot.isIncluded());
            vh.toggleButton.setEnabled(!slot.isDisabled());
            vh.toggleButton.setOnCheckedChangeListener(this);
            vh.toggleButton.setTag(vh);

            return view;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ViewHolder vh = (ViewHolder) buttonView.getTag();
            TimeSlotSelection slot = getItem(vh.position);
            if (slot.isDisabled()) return;
            if (isChecked) {
                slot.setState(TimeSlotSelection.State.INCLUDED);
            } else {
                slot.setState(TimeSlotSelection.State.EXCLUDED);
            }
        }

        private static class ViewHolder {
            ToggleButton toggleButton;
            int position;
        }

    }

}

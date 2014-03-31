package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TimeSlotsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TimeSlotsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TimeSlotsFragment extends Fragment {
    private static final String ARG_TIME_SLOTS = "time_slots";

    // TODO: Rename and change types of parameters
    private ArrayList<TimeSlotSelection> timeSlotSelections;
    private GridView timeSlotGrid;
    private TimeSlotSelectionArrayAdapter timeSlotAdapter;
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param timeSlotsSelections Time slot selections.
     * @return A new instance of fragment TimeSlotsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TimeSlotsFragment newInstance(List<TimeSlotSelection> timeSlotsSelections) {
        TimeSlotsFragment fragment = new TimeSlotsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_TIME_SLOTS, new ArrayList<TimeSlotSelection>(timeSlotsSelections));
        fragment.setArguments(args);
        return fragment;
    }

    public TimeSlotsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            timeSlotSelections = savedInstanceState.getParcelableArrayList(ARG_TIME_SLOTS);
        } else if (getArguments() != null) {
            timeSlotSelections = getArguments().getParcelableArrayList(ARG_TIME_SLOTS);
        } else {
            timeSlotSelections = new ArrayList<TimeSlotSelection>();
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    public static class TimeSlotSelectionArrayAdapter extends ArrayAdapter<TimeSlotSelection> implements CompoundButton.OnCheckedChangeListener {

        public TimeSlotSelectionArrayAdapter(Context context, List<TimeSlotSelection> selections) {
            super(context, R.layout.timeslot, selections);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.timeslot, null);
            }

            ToggleButton toggleButton = (ToggleButton) view.findViewById(R.id.time_slot_toggle);

            TimeSlotSelection slot = getItem(position);
            String text = slot.getBeginHour() + "h - " + slot.getEndHour() + "h";
            toggleButton.setTextOff(text);
            toggleButton.setTextOn(text);
            toggleButton.setChecked(slot.isIncluded());
            toggleButton.setOnCheckedChangeListener(this);

            view.setTag(position);

            return view;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int position = (Integer) ((View) buttonView.getParent()).getTag();
            TimeSlotSelection slot = getItem(position);
            if (isChecked) {
                slot.setState(TimeSlotToggleState.INCLUDED);
            } else {
                slot.setState(TimeSlotToggleState.EXCLUDED);
            }
        }
    }

}

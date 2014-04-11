package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartyListener;


/**
 * A fragment displaying the date of a party.
 */
public class DateFragment extends Fragment implements PartyListener {

    private TextView dateView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DateFragment.
     */
    public static DateFragment newInstance() {
        DateFragment fragment = new DateFragment();
        return fragment;
    }

    public DateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_date, container, false);

        dateView = (TextView) view.findViewById(R.id.party_date);

        return view;
    }


    @Override
    public void onPartyLoaded(Party party, User user) {
        Date date = new Date(party.getDate().getValue());
        String dateText;
        if (party.isPlanned()) {
            dateText = getString(R.string.format_date_and_time,
                    DateFormat.getDateFormat(getActivity()).format(date),
                    DateFormat.getTimeFormat(getActivity()).format(date));
        } else {
            dateText = getString(R.string.format_date_only,
                    DateFormat.getDateFormat(getActivity()).format(date));
        }
        dateView.setText(dateText);
    }

    @Override
    public void onPartyUnloaded() {
        dateView.setText("");
    }

}

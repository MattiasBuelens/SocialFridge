package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartyListener;


/**
 * A fragment displaying the place of a party.
 */
public class PlaceFragment extends Fragment implements PartyListener {

    private TextView placeView;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlaceFragment.
     */
    public static PlaceFragment newInstance() {
        PlaceFragment fragment = new PlaceFragment();
        return fragment;
    }

    public PlaceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_place, container, false);

        placeView = (TextView) view.findViewById(R.id.party_place);

        return view;
    }


    @Override
    public void onPartyLoaded(Party party, User user) {
        String hostName = party.getHost().getUserName();
        String placeText = getString(R.string.party_view_place, hostName);
        placeView.setText(placeText);
    }

    @Override
    public void onPartyUnloaded() {
        placeView.setText("");
    }

}

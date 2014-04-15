package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

import java.util.Date;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.dish.DishHeaderFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartyListener;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ArrayAdapter;

/**
 * Fragment displaying the general details of a party.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment implements PartyListener {

    private DishHeaderFragment dishHeader;
    private TextView dateView;
    private TextView placeView;
    private GridView partnersGrid;
    private PartnersListAdapter partnersAdapter;

    /**
     * Create a new partners fragment.
     *
     * @return A new instance of fragment DetailsFragment.
     */
    public static DetailsFragment newInstance() {
        DetailsFragment fragment = new DetailsFragment();
        return fragment;
    }

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_party_details, container, false);

        dishHeader = (DishHeaderFragment) getChildFragmentManager().findFragmentById(R.id.dish_header);
        dateView = ((TextView) view.findViewById(R.id.party_date));
        placeView = ((TextView) view.findViewById(R.id.party_place));
        partnersGrid = (GridView) view.findViewById(R.id.party_partners_list);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        partnersAdapter = new PartnersListAdapter(getActivity());
        partnersGrid.setAdapter(partnersAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPartyLoaded(Party party, User user) {
        updateDate(party);
        updatePlace(party);
        partnersAdapter.setData(party.getPartners());
    }

    @Override
    public void onPartyUnloaded() {
        clearDate();
        clearPlace();
        partnersAdapter.clear();
    }

    private void updateDate(Party party) {
        Date date = party.getDate();
        String dateText;
        if (party.isPlanned()) {
            // Fully planned, date and time available
            dateText = getString(R.string.format_date_and_time,
                    DateFormat.getDateFormat(getActivity()).format(date),
                    DateFormat.getTimeFormat(getActivity()).format(date));
        } else {
            // Not planned, date only
            dateText = getString(R.string.format_date_only,
                    DateFormat.getDateFormat(getActivity()).format(date));
        }
        dateView.setText(dateText);
    }

    private void clearDate() {
        dateView.setText("");
    }

    private void updatePlace(Party party) {
        String hostName = party.getHost().getUserName();
        String placeText = getString(R.string.party_view_place, hostName);
        placeView.setText(placeText);
    }

    private void clearPlace() {
        placeView.setText("");
    }

    public class PartnersListAdapter extends ArrayAdapter<PartyMember> {

        public PartnersListAdapter(Context context) {
            super(context, R.layout.partner_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.partner_list_item, null);
                vh = new ViewHolder();
                vh.pictureView = (ProfilePictureView) v.findViewById(R.id.partner_pic);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            PartyMember partner = getItem(position);
            vh.position = position;
            vh.pictureView.setProfileId(partner.getUserID());

            return v;
        }

        private class ViewHolder {
            ProfilePictureView pictureView;
            int position;
        }

    }

}

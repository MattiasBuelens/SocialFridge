package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.facebook.widget.ProfilePictureView;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartyListener;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ArrayAdapter;

/**
 * Fragment displaying the partners in a party.
 * Use the {@link PartnersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PartnersFragment extends Fragment implements PartyListener {

    private GridView partnersGrid;
    private PartnersListAdapter partnersAdapter;

    /**
     * Create a new partners fragment.
     *
     * @return A new instance of fragment PartnersFragment.
     */
    public static PartnersFragment newInstance() {
        PartnersFragment fragment = new PartnersFragment();
        return fragment;
    }

    public PartnersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_party_partners, container, false);

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
        partnersAdapter.setData(party.getPartners());
    }

    @Override
    public void onPartyUnloaded() {
        partnersAdapter.clear();
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

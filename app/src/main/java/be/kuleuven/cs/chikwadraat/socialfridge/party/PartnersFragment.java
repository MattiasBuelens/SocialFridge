package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.ArrayAdapter;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Fragment displaying the partners in a party.
 * Use the {@link PartnersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PartnersFragment extends Fragment implements PartyListener {

    private PartnersListAdapter partnersAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        partnersAdapter = new PartnersListAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_party_partners, container, false);

        GridView partnersGrid = (GridView) view.findViewById(R.id.party_partners_list);
        partnersGrid.setAdapter(partnersAdapter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // TODO Check if Activity implements listener and store listener
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // TODO Clear listener
    }

    @Override
    public void onPartyLoaded(Party party, User user) {
        // Filter on partners
        List<PartyMember> partners = getPartners(party.getMembers());
        partnersAdapter.setData(partners);
    }

    @Override
    public void onPartyUnloaded() {
        partnersAdapter.clear();
    }

    private List<PartyMember> getPartners(List<PartyMember> members) {
        List<PartyMember> partners = new ArrayList<PartyMember>(members.size());
        for (PartyMember member : members) {
            if (member.getInParty()) {
                partners.add(member);
            }
        }
        return partners;
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
            vh.pictureView.setProfileId(partner.getUserID());

            return v;
        }

        private class ViewHolder {
            ProfilePictureView pictureView;
            int position;
        }

    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.widget.ProfilePictureView;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.dish.DishHeaderFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.ChecklistItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.DishItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartyListener;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Fragment displaying the general details of a party.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment implements PartyListener {

    private DishHeaderFragment dishHeader;
    private TextView dateView;
    private TextView placeView;
    private TextView partnersTitle;
    private GridView partnersView;
    private ListView checklistView;
    private PartnersListAdapter partnersAdapter;
    private ChecklistAdapter checklistAdapter;

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

        dishHeader = (DishHeaderFragment) getFragmentManager().findFragmentById(R.id.dish_header);

        dateView = (TextView) view.findViewById(R.id.party_date);
        placeView = (TextView) view.findViewById(R.id.party_place);
        partnersTitle = (TextView) view.findViewById(R.id.party_partners);
        partnersView = (GridView) view.findViewById(R.id.party_partners_list);
        checklistView = (ListView) view.findViewById(R.id.party_checklist);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        partnersAdapter = new PartnersListAdapter(getActivity());
        partnersView.setAdapter(partnersAdapter);

        checklistAdapter = new ChecklistAdapter(getActivity());
        checklistView.setAdapter(checklistAdapter);
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
        updateDish(party);
        updateDate(party);
        updatePlace(party);
        updatePartners(party);
        updateChecklist(party, user);
    }

    @Override
    public void onPartyUnloaded() {
        clearDish();
        clearDate();
        clearPlace();
        clearPartners();
        clearChecklist();
    }

    private void updateDish(Party party) {
        dishHeader.setDish(party.getDish());
    }

    private void clearDish() {
        dishHeader.setDish(null);
    }

    private void updateDate(Party party) {
        dateView.setText(party.formatDate(getActivity()));
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

    private void updatePartners(Party party) {
        int nbOtherPartners = party.getPartners().size() - 1;
        PartyMember host = party.getHost();
        String partnersText = getResources().getQuantityString(R.plurals.party_partners, nbOtherPartners,
                host.getUserName(), nbOtherPartners);
        partnersTitle.setText(partnersText);
        AdapterUtils.setAll(partnersAdapter, party.getPartners());
    }

    private void clearPartners() {
        partnersTitle.setText("");
        partnersAdapter.clear();
    }

    private void updateChecklist(Party party, User user) {
        checklistAdapter.userPartner = party.getPartner(user);
        AdapterUtils.setAll(checklistAdapter, party.getChecklist());
    }

    private void clearChecklist() {
        checklistAdapter.userPartner = null;
        checklistAdapter.clear();
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

    public class ChecklistAdapter extends ArrayAdapter<ChecklistItem> {

        private PartyMember userPartner;

        public ChecklistAdapter(Context context) {
            super(context, R.layout.party_checklist_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.party_checklist_list_item, null);
                vh = new ViewHolder(v);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            ChecklistItem item = getItem(position);

            // Check if available amount is sufficient
            boolean isSufficient = item.getBring().compareTo(item.getRequired()) >= 0;

            // Check if current user needs to bring this item
            boolean bring = false;
            String bringText = "";
            if (userPartner != null) {
                DishItem bringItem = userPartner.getBringItem(item.getIngredient().getID());
                if (bringItem != null && bringItem.getMeasure().getValue() > 0) {
                    bring = true;
                    bringText = getString(R.string.party_checklist_bring, bringItem.getMeasure().toString());
                }
            }

            vh.position = position;
            vh.pictureView.setImageUrl(item.getIngredient().getThumbnailURL(), Application.get().getImageLoader());
            vh.nameView.setText(item.getIngredient().getName());
            vh.requiredQuantityView.setText(getString(R.string.party_checklist_needed, item.getRequired().toString()));
            vh.availableQuantityView.setText(getString(R.string.party_checklist_available, item.getBring().toString()));
            vh.availableQuantityView.setTextColor(getResources().getColor(isSufficient
                    ? R.color.party_checklist_item_sufficient
                    : R.color.party_checklist_item_insufficient));
            vh.bringQuantityView.setVisibility(bring ? View.VISIBLE : View.GONE);
            vh.bringQuantityView.setText(bringText);

            return v;
        }

        private class ViewHolder {
            final TextView nameView;
            final NetworkImageView pictureView;
            final TextView requiredQuantityView;
            final TextView availableQuantityView;
            final TextView bringQuantityView;
            int position;

            private ViewHolder(View v) {
                nameView = (TextView) v.findViewById(R.id.ingredient_name);
                pictureView = (NetworkImageView) v.findViewById(R.id.ingredient_pic);
                requiredQuantityView = (TextView) v.findViewById(R.id.item_quantity_required);
                availableQuantityView = (TextView) v.findViewById(R.id.item_quantity_available);
                bringQuantityView = (TextView) v.findViewById(R.id.item_quantity_bring);
            }
        }

    }

}

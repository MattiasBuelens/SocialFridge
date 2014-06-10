package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.widget.ProfilePictureView;

import java.util.ArrayList;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.dish.DishHeaderFragment;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.DishItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
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

    private static final String STATE_CHECKLIST = "checklist";

    private DishHeaderFragment dishHeader;
    private TextView dateView;
    private TextView placeView;
    private TextView partnersView;
    private GridView partnersGrid;
    private ListView checklistView;
    private PartnersListAdapter partnersAdapter;
    private ChecklistAdapter checklistAdapter;

    private ArrayList<ChecklistItem> checklistItems = new ArrayList<ChecklistItem>();

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore
            checklistItems = savedInstanceState.getParcelableArrayList(STATE_CHECKLIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_party_details, container, false);

        dishHeader = (DishHeaderFragment) getFragmentManager().findFragmentById(R.id.dish_header);

        dateView = (TextView) view.findViewById(R.id.party_date);
        placeView = (TextView) view.findViewById(R.id.party_place);
        partnersView = (TextView) view.findViewById(R.id.party_partners);
        partnersGrid = (GridView) view.findViewById(R.id.party_partners_list);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        partnersAdapter = new PartnersListAdapter(getActivity());
        partnersGrid.setAdapter(partnersAdapter);

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
        updateChecklist(party);
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
        partnersView.setText(partnersText);
        AdapterUtils.setAll(partnersAdapter, party.getPartners());
    }

    private void clearPartners() {
        partnersView.setText("");
        partnersAdapter.clear();
    }

    private void updateChecklist(Party party) {
        checklistItems.clear();
        for (Ingredient ingredient : party.getDish().getIngredients()) {
            DishItem required = party.getRequiredItem(ingredient.getID());
            DishItem bring = party.getBringItem(ingredient.getID());
            DishItem missing = party.getMissingItem(ingredient.getID());
            ChecklistItem item = new ChecklistItem(ingredient, required, bring, missing);
            checklistItems.add(item);
        }
        checklistAdapter.notifyDataSetChanged();
    }

    private void clearChecklist() {
        checklistAdapter.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_CHECKLIST, checklistItems);
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
            boolean isInParty = item.bring.getMeasure().compareTo(item.required.getMeasure()) <= 0;

            vh.position = position;
            vh.pictureView.setImageUrl(item.ingredient.getThumbnailURL(), Application.get().getImageLoader());
            vh.nameView.setText(item.ingredient.getName());
            vh.requiredQuantityView.setText(item.required.getMeasure().toString());
            vh.bringQuantityView.setText(item.bring.getMeasure().toString());
            vh.inPartyView.setImageResource(isInParty
                    ? R.drawable.abc_ic_cab_done_holo_light
                    : R.drawable.ic_action_cancel_light);

            return v;
        }

        private class ViewHolder {
            TextView nameView;
            NetworkImageView pictureView;
            TextView requiredQuantityView;
            TextView bringQuantityView;
            ImageView inPartyView;
            int position;

            private ViewHolder(View v) {
                nameView = (TextView) v.findViewById(R.id.ingredient_name);
                pictureView = (NetworkImageView) v.findViewById(R.id.ingredient_pic);
                requiredQuantityView = (TextView) v.findViewById(R.id.item_quantity_required);
                bringQuantityView = (TextView) v.findViewById(R.id.item_quantity_bring);
                inPartyView = (ImageView) v.findViewById(R.id.item_in_party);
            }
        }

    }

    public static class ChecklistItem implements Parcelable {

        public final Ingredient ingredient;
        public final DishItem required;
        public final DishItem bring;
        public final DishItem missing;

        public ChecklistItem(Ingredient ingredient, DishItem required, DishItem bring, DishItem missing) {
            this.ingredient = ingredient;
            this.required = required;
            this.bring = bring;
            this.missing = missing;
        }

        public ChecklistItem(Parcel in) {
            this.ingredient = in.readParcelable(Ingredient.class.getClassLoader());
            this.required = in.readParcelable(DishItem.class.getClassLoader());
            this.bring = in.readParcelable(DishItem.class.getClassLoader());
            this.missing = in.readParcelable(DishItem.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(ingredient, 0);
            dest.writeParcelable(required, 0);
            dest.writeParcelable(bring, 0);
            dest.writeParcelable(missing, 0);
        }

        public static final Creator<ChecklistItem> CREATOR = new Creator<ChecklistItem>() {

            public ChecklistItem createFromParcel(Parcel in) {
                return new ChecklistItem(in);
            }

            public ChecklistItem[] newArray(int size) {
                return new ChecklistItem[size];
            }

        };

    }

}

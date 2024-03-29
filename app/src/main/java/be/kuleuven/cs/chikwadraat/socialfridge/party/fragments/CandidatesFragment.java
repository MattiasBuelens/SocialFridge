package be.kuleuven.cs.chikwadraat.socialfridge.party.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyCandidatesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.DishItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartyListener;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Fragment displaying the candidates for a party.
 * Use the {@link CandidatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CandidatesFragment extends Fragment implements PartyListener {

    private static final String TAG = "CandidatesFragment";

    private static final int LOADER_CANDIDATES = 1;
    private static final String LOADER_ARGS_PARTY_ID = "party_id";

    private View candidatesHeader;
    private ListView candidatesList;
    private CandidatesListAdapter candidatesAdapter;
    private CandidatesLoaderCallbacks loaderCallbacks = new CandidatesLoaderCallbacks();
    private CandidateListener listener;

    /**
     * Create a new candidates fragment.
     *
     * @return A new instance of fragment CandidatesFragment.
     */
    public static CandidatesFragment newInstance() {
        CandidatesFragment fragment = new CandidatesFragment();
        return fragment;
    }

    public CandidatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_party_candidates, container, false);

        candidatesList = (ListView) view.findViewById(R.id.party_candidates_list);
        candidatesHeader = view.findViewById(R.id.party_candidates_header);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addHeaderView(candidatesHeader);

        candidatesAdapter = new CandidatesListAdapter(getActivity());
        candidatesList.setAdapter(candidatesAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (CandidateListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CandidateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onPartyLoaded(Party party, User user) {
        // Load candidates if host
        if (party.isHost(user)) {
            loadCandidates(party.getID());
        }
    }

    @Override
    public void onPartyUnloaded() {
        candidatesAdapter.clear();
        getLoaderManager().destroyLoader(LOADER_CANDIDATES);
    }

    public void addHeaderView(View headerView) {
        if (headerView.getParent() != null) {
            // Remove from parent
            ((ViewGroup) headerView.getParent()).removeView(headerView);
            // Reset layout params
            headerView.setLayoutParams(new AbsListView.LayoutParams(headerView.getLayoutParams()));
        }
        candidatesList.addHeaderView(headerView);
    }

    private void loadCandidates(long partyID) {
        Bundle args = new Bundle();
        args.putLong(LOADER_ARGS_PARTY_ID, partyID);
        getLoaderManager().restartLoader(LOADER_CANDIDATES, args, loaderCallbacks);
    }

    private String formatCandidateItems(List<DishItem> bringItems) {
        return Joiner.on('\n').join(Collections2.transform(bringItems, new Function<DishItem, String>() {
            @Override
            public String apply(DishItem bringItem) {
                return formatCandidateItem(bringItem);
            }
        }));
    }

    private String formatCandidateItem(DishItem bringItem) {
        return getString(R.string.party_candidate_item_format,
                bringItem.getMeasure().toString(),
                bringItem.getIngredient().getName());
    }

    public void refreshCandidates() {
        if (candidatesAdapter != null) {
            candidatesAdapter.notifyDataSetChanged();
        }
    }

    private void invite(PartyMember candidate) {
        if (listener != null) {
            listener.onCandidateInvited(candidate);
        }
    }

    private void cancelInvite(PartyMember candidate) {
        if (listener != null) {
            listener.onCandidateInviteCanceled(candidate);
        }
    }

    public interface CandidateListener {

        public void onCandidateInvited(PartyMember candidate);

        public void onCandidateInviteCanceled(PartyMember candidate);

    }

    public class CandidatesListAdapter extends ArrayAdapter<PartyMember> implements View.OnClickListener {

        public CandidatesListAdapter(Context context) {
            super(context, R.layout.candidate_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.candidate_list_item, null);
                vh = new ViewHolder(v);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            PartyMember candidate = getItem(position);
            vh.position = position;
            vh.pictureView.setProfileId(candidate.getUserID());
            vh.nameView.setText(candidate.getUserName());
            vh.itemsView.setText(formatCandidateItems(candidate.getBringItems()));
            vh.inviteButton.setOnClickListener(this);
            vh.inviteButton.setTag(vh);

            if (candidate.isInvited()) {
                vh.inviteButton.setText(R.string.party_partner_status_invited);
                vh.inviteButton.setEnabled(false);
                //cancelInviteButton.setVisibility(View.VISIBLE);
            } else {
                vh.inviteButton.setText(R.string.party_action_invite);
                vh.inviteButton.setEnabled(true);
                //cancelInviteButton.setVisibility(View.INVISIBLE);
            }

            return v;
        }

        @Override
        public void onClick(View v) {
            ViewHolder vh = (ViewHolder) v.getTag();
            PartyMember candidate = getItem(vh.position);
            switch (v.getId()) {
                case R.id.candidate_invite:
                    invite(candidate);
                    break;
//                case R.id.candidate_cancel_invite:
//                    cancelInvite(candidate);
//                    break;
            }
        }

        private class ViewHolder {
            final ProfilePictureView pictureView;
            final TextView nameView;
            final TextView itemsView;
            final Button inviteButton;
            int position;

            private ViewHolder(View v) {
                pictureView = (ProfilePictureView) v.findViewById(R.id.candidate_pic);
                nameView = (TextView) v.findViewById(R.id.candidate_name);
                itemsView = (TextView) v.findViewById(R.id.candidate_items);
                inviteButton = (Button) v.findViewById(R.id.candidate_invite);
                //cancelInviteButton = (Button) v.findViewById(R.id.candidate_cancel_invite);
            }
        }

    }

    private class CandidatesLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<PartyMember>> {

        @Override
        public Loader<List<PartyMember>> onCreateLoader(int id, Bundle args) {
            long partyID = args.getLong(LOADER_ARGS_PARTY_ID);
            return new PartyCandidatesLoader(getActivity(), partyID);
        }

        @Override
        public void onLoadFinished(Loader<List<PartyMember>> loader, List<PartyMember> candidates) {
            AdapterUtils.setAll(candidatesAdapter, candidates);
        }

        @Override
        public void onLoaderReset(Loader<List<PartyMember>> loader) {
            candidatesAdapter.clear();
        }

    }

}

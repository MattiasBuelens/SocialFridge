package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.ArrayAdapter;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyCandidatesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Fragment displaying the candidates for a party.
 * Use the {@link be.kuleuven.cs.chikwadraat.socialfridge.party.CandidatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CandidatesFragment extends ListFragment implements PartyListener {

    private static final String TAG = "CandidatesFragment";

    private static final int LOADER_CANDIDATES = 1;
    private static final String LOADER_ARGS_PARTY_ID = "party_id";

    private CandidatesListAdapter candidatesAdapter;
    private CandidateListener listener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PartnersFragment.
     */
    public static CandidatesFragment newInstance() {
        CandidatesFragment fragment = new CandidatesFragment();
        return fragment;
    }

    public CandidatesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        candidatesAdapter = new CandidatesListAdapter(getActivity());
        setListAdapter(candidatesAdapter);
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
        // TODO Clear listener
    }

    @Override
    public void onPartyLoaded(Party party, User user) {
        // Load candidates if host
        if (user.getId().equals(party.getHostID())) {
            loadCandidates(party.getId());
        }
    }

    @Override
    public void onPartyUnloaded() {
        candidatesAdapter.clear();
        getLoaderManager().destroyLoader(LOADER_CANDIDATES);
    }

    private void loadCandidates(long partyID) {
        Bundle args = new Bundle();
        args.putLong(LOADER_ARGS_PARTY_ID, partyID);
        getLoaderManager().restartLoader(LOADER_CANDIDATES, args, new CandidatesLoaderCallbacks());
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
                vh = new ViewHolder();
                vh.pictureView = (ProfilePictureView) v.findViewById(R.id.candidate_pic);
                vh.nameView = (TextView) v.findViewById(R.id.candidate_name);
                vh.inviteButton = (Button) v.findViewById(R.id.candidate_invite);
                //vh.cancelInviteButton = (Button) v.findViewById(R.id.candidate_cancel_invite);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            PartyMember candidate = getItem(position);
            vh.position = position;
            vh.pictureView.setProfileId(candidate.getUserID());
            vh.nameView.setText(candidate.getUserName());
            vh.inviteButton.setOnClickListener(this);
            vh.inviteButton.setTag(vh);

            if (candidate.getInvited()) {
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
            ProfilePictureView pictureView;
            TextView nameView;
            Button inviteButton;
            int position;
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
            candidatesAdapter.setData(candidates);
        }

        @Override
        public void onLoaderReset(Loader<List<PartyMember>> loader) {
            candidatesAdapter.clear();
        }

    }

}

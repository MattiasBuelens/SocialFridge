package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Party activity.
 * <p/>
 * Invite partners
 */
public class PartyActivity extends BaseActivity {

    private static final String TAG = "PartyActivity";

    @Override
    protected void onAfterCreate(Bundle savedInstanceState) {
        setContentView(R.layout.party);

        TwoWayView partnersList = (TwoWayView) findViewById(R.id.party_partners_list);

        // TODO Replace dummy data
        User user1 = new User();
        user1.setId("839285021");
        user1.setName("Mattias Buelens");
        User user2 = new User();
        user2.setId("100002604113528");
        user2.setName("Vital D'haveloose");
        User user3 = new User();
        user3.setId("740193939");
        user3.setName("Milan Samyn");
        List<User> users = ImmutableList.of(user1, user2, user3);

        partnersList.setAdapter(new PartnersListAdapter(this, users));

        ListView candidatesList = (ListView) findViewById(R.id.party_candidates_list);

        candidatesList.setAdapter(new CandidatesListAdapter(this, users));
    }

    public static class PartnersListAdapter extends ArrayAdapter<User> {

        public PartnersListAdapter(Context context, User[] partners) {
            this(context, Arrays.asList(partners));
        }

        public PartnersListAdapter(Context context, List<User> partners) {
            super(context, R.layout.partner_list_item, partners);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.partner_list_item, null);
            }

            ProfilePictureView pictureView = (ProfilePictureView) view.findViewById(R.id.partner_pic);

            User user = getItem(position);
            pictureView.setProfileId(user.getId());

            return view;
        }
    }

    public static class CandidatesListAdapter extends ArrayAdapter<User> {

        public CandidatesListAdapter(Context context, User[] candidates) {
            this(context, Arrays.asList(candidates));
        }

        public CandidatesListAdapter(Context context, List<User> candidates) {
            super(context, R.layout.candidate_list_item, candidates);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = View.inflate(getContext(), R.layout.candidate_list_item, null);
            }

            ProfilePictureView pictureView = (ProfilePictureView) view.findViewById(R.id.candidate_pic);
            TextView nameView = (TextView) view.findViewById(R.id.candidate_name);
            Button inviteButton = (Button) view.findViewById(R.id.candidate_invite);
            Button cancelInviteButton = (Button) view.findViewById(R.id.candidate_cancel_invite);

            User user = getItem(position);
            pictureView.setProfileId(user.getId());
            nameView.setText(user.getName());

            // TODO Dummy condition
            if((user.hashCode() & 1) == 1) {
                inviteButton.setText(R.string.party_partner_status_invited);
                inviteButton.setEnabled(false);
                cancelInviteButton.setVisibility(View.VISIBLE);
            } else {
                inviteButton.setText(R.string.party_action_invite);
                inviteButton.setEnabled(true);
                cancelInviteButton.setVisibility(View.INVISIBLE);
            }

            return view;
        }
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.ListActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartiesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Parties activity.
 */
public class PartiesActivity extends ListActivity {

    private static final String TAG = "PartiesActivity";

    private static final int LOADER_PARTIES = 1;

    private PartiesArrayAdapter partiesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_card_list);

        partiesArrayAdapter = new PartiesArrayAdapter();
        setListAdapter(partiesArrayAdapter);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        getSupportLoaderManager().restartLoader(LOADER_PARTIES, null, new PartiesLoaderCallbacks());
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        getSupportLoaderManager().destroyLoader(LOADER_PARTIES);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Party party = (Party) l.getItemAtPosition(position);
        Intent intent = new Intent(this, ViewPartyActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, party.getID());
        startActivity(intent);
    }

    public class PartiesArrayAdapter extends ArrayAdapter<Party> {

        public PartiesArrayAdapter() {
            super(PartiesActivity.this, R.layout.party_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.party_list_item, null);
                vh = new ViewHolder(v);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            Party party = getItem(position);
            PartyMember host = party.getHost();
            PartyMember user = party.getPartner(getLoggedInUser());
            int nbOtherPartners = party.getPartners().size() - 1;

            String othersText = getContext().getResources().getQuantityString(R.plurals.party_list_partners, nbOtherPartners, nbOtherPartners);
            String partnersText = getContext().getString(R.string.party_list_members, host.getUserName(), othersText);
            String roleText = getContext().getString(user.getRole().getStringResource());
            int roleColor = getContext().getResources().getColor(user.getRole().getColorResource());
            String statusText = getContext().getString(party.getStatus().getStringResource());
            int statusColor = getContext().getResources().getColor(party.getStatus().getColorResource());
            String dishText = "Spaghetti Bolognese"; // TODO Dummy
            Drawable dishDrawable = getContext().getResources().getDrawable(R.drawable.detail_spaghetti); // TODO Dummy

            vh.position = position;
            vh.hostPictureView.setProfileId(party.getHostID());
            vh.partnersView.setText(partnersText);
            vh.roleView.setText(roleText);
            vh.roleView.setBackgroundColor(roleColor);
            vh.statusView.setText(statusText);
            vh.statusView.setBackgroundColor(statusColor);
            vh.dishImageView.setImageDrawable(dishDrawable);
            vh.dishNameView.setText(dishText);

            return v;
        }

        private class ViewHolder {
            final ProfilePictureView hostPictureView;
            final TextView partnersView;
            final TextView roleView;
            final TextView statusView;
            final ImageView dishImageView;
            final TextView dishNameView;
            int position;

            private ViewHolder(View v) {
                hostPictureView = (ProfilePictureView) v.findViewById(R.id.host_pic);
                partnersView = (TextView) v.findViewById(R.id.partners);
                roleView = (TextView) v.findViewById(R.id.party_role);
                statusView = (TextView) v.findViewById(R.id.party_status);
                dishImageView = (ImageView) v.findViewById(R.id.dish_pic);
                dishNameView = (TextView) v.findViewById(R.id.dish_name);
            }
        }

    }

    private class PartiesLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Party>> {

        @Override
        public Loader<List<Party>> onCreateLoader(int id, Bundle args) {
            return new PartiesLoader(PartiesActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<Party>> loader, List<Party> parties) {
            AdapterUtils.setAll(partiesArrayAdapter, parties);
        }

        @Override
        public void onLoaderReset(Loader<List<Party>> loader) {
            partiesArrayAdapter.clear();
        }

    }

}

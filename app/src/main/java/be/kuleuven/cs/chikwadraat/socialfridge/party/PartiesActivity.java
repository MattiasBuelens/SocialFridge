package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.facebook.Session;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Application;
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
public class PartiesActivity extends ListActivity implements View.OnClickListener {

    private static final String TAG = "PartiesActivity";

    private static final int LOADER_PARTIES = 1;

    private PartiesArrayAdapter partiesArrayAdapter;
    private PartiesLoaderCallbacks loaderCallbacks = new PartiesLoaderCallbacks();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_list);

        findViewById(R.id.action_create_party).setOnClickListener(this);

        partiesArrayAdapter = new PartiesArrayAdapter();
        setListAdapter(partiesArrayAdapter);
        setListShownNoAnimation(false);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        setListShown(false);
        getSupportLoaderManager().restartLoader(LOADER_PARTIES, null, loaderCallbacks);
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        setListShown(false);
        getSupportLoaderManager().destroyLoader(LOADER_PARTIES);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Party party = (Party) l.getItemAtPosition(position);
        Intent intent = new Intent(this, ViewPartyActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, party.getID());
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_create_party: {
                Intent intent = new Intent(this, CreatePartyActivity.class);
                startActivity(intent);
            }
            break;
        }
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
            PartyMember user = party.getMember(getLoggedInUser());
            int nbOtherPartners = party.getPartners().size() - 1;

            String partnersText = getContext().getResources().getQuantityString(R.plurals.party_partners, nbOtherPartners,
                    host.getUserName(), nbOtherPartners);
            String roleText = getContext().getString(user.getRole().getStringResource());
            int roleColor = getContext().getResources().getColor(user.getRole().getColorResource());
            String statusText = getContext().getString(party.getStatus().getStringResource());
            int statusColor = getContext().getResources().getColor(party.getStatus().getColorResource());
            String dateText = party.formatDate(getContext());
            String dishText = party.getDish().getName();
            String dishThumbnailURL = party.getDish().getThumbnailURL();

            vh.position = position;
            vh.partnersView.setText(partnersText);
            vh.roleView.setText(roleText);
            vh.roleView.setBackgroundColor(roleColor);
            vh.statusView.setText(statusText);
            vh.statusView.setBackgroundColor(statusColor);
            vh.dateView.setText(dateText);
            vh.dishImageView.setImageUrl(dishThumbnailURL, Application.get().getImageLoader());
            vh.dishNameView.setText(dishText);

            return v;
        }

        private class ViewHolder {
            final TextView partnersView;
            final TextView roleView;
            final TextView statusView;
            final TextView dateView;
            final NetworkImageView dishImageView;
            final TextView dishNameView;
            int position;

            private ViewHolder(View v) {
                partnersView = (TextView) v.findViewById(R.id.party_partners);
                roleView = (TextView) v.findViewById(R.id.party_role);
                statusView = (TextView) v.findViewById(R.id.party_status);
                dateView = (TextView) v.findViewById(R.id.party_date);
                dishImageView = (NetworkImageView) v.findViewById(R.id.dish_pic);
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
            setListShown(true);
        }

        @Override
        public void onLoaderReset(Loader<List<Party>> loader) {
            partiesArrayAdapter.clear();
        }

    }

}

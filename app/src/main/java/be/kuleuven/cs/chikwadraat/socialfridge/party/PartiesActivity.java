package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.ListActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartiesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ArrayAdapter;

/**
 * Parties activity.
 */
public class PartiesActivity extends ListActivity {

    private static final String TAG = "PartiesActivity";

    private static final int LOADER_PARTIES = 1;

    private PartiesArrayAdapter partiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        partiesAdapter = new PartiesArrayAdapter(this);
        setListAdapter(partiesAdapter);
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
        // TODO View party
        Intent intent = new Intent(this, PartyInviteActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, party.getId());
        startActivity(intent);
    }

    public static class PartiesArrayAdapter extends ArrayAdapter<Party> {

        public PartiesArrayAdapter(Context context) {
            super(context, R.layout.party_list_item);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.party_list_item, null);
                vh = new ViewHolder();
                vh.hostPictureView = (ProfilePictureView) v.findViewById(R.id.host_pic);
                vh.partnersView = (TextView) v.findViewById(R.id.partners);
                vh.dishView = (TextView) v.findViewById(R.id.dish_name);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            Party party = getItem(position);
            PartyMember host = PartyUtils.getHost(party);
            int nbOtherPartners = party.getPartners().size() - 1;

            String othersText = getContext().getResources().getQuantityString(R.plurals.party_list_partners, nbOtherPartners, nbOtherPartners);
            String partnersText = getContext().getString(R.string.party_list_members, host.getUserName(), othersText);

            vh.position = position;
            vh.hostPictureView.setProfileId(party.getHostID());
            vh.partnersView.setText(partnersText);
            vh.dishView.setText("Making Spaghetti");

            return v;
        }

        private class ViewHolder {
            ProfilePictureView hostPictureView;
            TextView partnersView;
            TextView dishView;
            int position;
        }

    }

    private class PartiesLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Party>> {

        @Override
        public Loader<List<Party>> onCreateLoader(int id, Bundle args) {
            return new PartiesLoader(PartiesActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<Party>> loader, List<Party> parties) {
            partiesAdapter.setData(parties);
        }

        @Override
        public void onLoaderReset(Loader<List<Party>> loader) {
            partiesAdapter.clear();
        }

    }

}

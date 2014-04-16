package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import be.kuleuven.cs.chikwadraat.socialfridge.ListActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartiesLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;
import be.kuleuven.cs.chikwadraat.socialfridge.util.SectionedAdapter;

/**
 * Parties activity.
 */
public class PartiesActivity extends ListActivity {

    private static final String TAG = "PartiesActivity";

    private static final int LOADER_PARTIES = 1;

    private PartiesArrayAdapter partiesArrayAdapter;
    private PartiesAdapter partiesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_card_list);

        partiesArrayAdapter = new PartiesArrayAdapter(this);
        partiesAdapter = new PartiesAdapter(partiesArrayAdapter);
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
        Intent intent = new Intent(this, ViewPartyActivity.class);
        intent.putExtra(BasePartyActivity.EXTRA_PARTY_ID, party.getID());
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
            PartyMember host = party.getHost();
            int nbOtherPartners = party.getPartners().size() - 1;

            String othersText = getContext().getResources().getQuantityString(R.plurals.party_list_partners, nbOtherPartners, nbOtherPartners);
            String partnersText = getContext().getString(R.string.party_list_members, host.getUserName(), othersText);

            vh.position = position;
            vh.hostPictureView.setProfileId(party.getHostID());
            vh.partnersView.setText(partnersText);
            //vh.dishView.setText("Making Spaghetti");

            return v;
        }

        private class ViewHolder {
            ProfilePictureView hostPictureView;
            TextView partnersView;
            TextView dishView;
            int position;
        }

    }

    public static class PartiesAdapter extends SectionedAdapter<PartiesArrayAdapter, Party.Status> {

        private static final Ordering<Party.Status> statusOrdering = new Ordering<Party.Status>() {
            @Override
            public int compare(@Nullable Party.Status left, @Nullable Party.Status right) {
                return -Ints.compare(left.ordinal(), right.ordinal());
            }
        };

        private final Context context;

        public PartiesAdapter(PartiesArrayAdapter sourceAdapter) {
            super(sourceAdapter);
            this.context = sourceAdapter.getContext();
        }

        protected Context getContext() {
            return context;
        }

        @Override
        protected Party.Status getSectionKey(int sourcePosition) {
            return getSource().getItem(sourcePosition).getStatus();
        }

        @Override
        protected Comparator<Party.Status> getSectionOrdering() {
            return statusOrdering;
        }

        @Override
        protected View getHeaderView(Party.Status status, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), R.layout.party_list_status_header, null);
                vh = new ViewHolder();
                vh.titleView = (TextView) v.findViewById(R.id.text1);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            String titleText = getContext().getString(status.getStringResource());

            vh.status = status;
            vh.titleView.setText(titleText);

            return v;
        }

        private class ViewHolder {
            TextView titleView;
            Party.Status status;
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

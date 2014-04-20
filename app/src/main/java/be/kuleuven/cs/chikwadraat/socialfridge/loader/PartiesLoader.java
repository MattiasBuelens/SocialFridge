package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Users;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;

/**
 * Retrieves a user's parties.
 */
public class PartiesLoader extends BaseLoader<List<Party>> {

    private static final String TAG = "PartiesLoader";

    public PartiesLoader(Context context) {
        super(context);
    }

    @Override
    public List<Party> loadInBackground() {
        Users users = Endpoints.users(getContext());
        Session session = Session.getActiveSession();

        try {
            return Party.fromEndpoint(users.getParties(session.getAccessToken()).execute().getItems());
        } catch (IOException e) {
            Log.e(TAG, "Error while loading parties: " + e.getMessage());
            trackException(e);
            return null;
        }
    }

}

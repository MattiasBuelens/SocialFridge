package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.util.Log;

import com.facebook.Session;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.parties.model.Party;

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
        Parties parties = Endpoints.parties(getContext());
        Session session = Session.getActiveSession();

        try {
            return parties.getParties(session.getAccessToken()).execute().getList();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

}

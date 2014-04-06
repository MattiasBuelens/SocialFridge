package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;

import be.kuleuven.cs.chikwadraat.socialfridge.parties.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.users.Users;

/**
 * Endpoints access.
 */
public class Endpoints {

    /**
     * Testing flag.
     * TODO Set to false on release!
     */
    private static final boolean TESTING = false;

    /**
     * Endpoint root URL for testing setup.
     * TODO Replace with own IP for testing
     */
    private static final String TEST_ROOT_URL = "http://192.168.1.163:8080/_ah/api/";

    public static Users users(Context context) {
        Users.Builder builder = new Users.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
        return prepare(builder, context).build();
    }

    public static Parties parties(Context context) {
        Parties.Builder builder = new Parties.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
        return prepare(builder, context).build();
    }

    private static <T extends AbstractGoogleJsonClient.Builder> T prepare(T builder, Context context) {
        builder.setApplicationName(context.getString(R.string.app_name));
        if (TESTING) {
            builder.setRootUrl(TEST_ROOT_URL);
        }
        return builder;
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Parties;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Users;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Dishes;

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
        return endpoint(context).users();
    }

    public static Parties parties(Context context) {
        return endpoint(context).parties();
    }

    public static Dishes dishes(Context context) {
        return endpoint(context).dishes();
    }

    private static Endpoint endpoint(Context context) {
        Endpoint.Builder builder = new Endpoint.Builder(newHttpTransport(), getJsonFactory(), null);
        return prepare(builder, context).build();
    }

    private static HttpTransport newHttpTransport() {
        return AndroidHttp.newCompatibleTransport();
    }

    private static JsonFactory getJsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    private static <T extends AbstractGoogleJsonClient.Builder> T prepare(T builder, Context context) {
        builder.setApplicationName(context.getString(R.string.app_name));
        if (TESTING) {
            builder.setRootUrl(TEST_ROOT_URL);
        }
        return builder;
    }

}

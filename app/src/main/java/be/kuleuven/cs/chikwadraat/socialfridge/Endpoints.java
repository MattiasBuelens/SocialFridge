package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;

import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;

/**
 * Helper class for endpoints
 */
public class Endpoints {

    /**
     * Testing flag.
     * TODO Set to false on release!
     */
    private static final boolean TESTING = true;

    /**
     * Endpoint root URL for testing setup.
     * TODO Replace with own IP for testing
     */
    private static final String TEST_ROOT_URL = "http://192.168.0.156:8080/_ah/api/";

    public static <T extends AbstractGoogleJsonClient.Builder> T prepare(T builder, Context context) {
        builder.setApplicationName(context.getString(R.string.app_name));
        if (TESTING) {
            builder.setRootUrl(TEST_ROOT_URL);
        }
        return builder;
    }

}

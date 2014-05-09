package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClient;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint;

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
     * Endpoint root URL for production setup.
     * TODO Remove version prefix on release!
     */
    private static final String PRODUCTION_ROOT_URL = "https://socialfridge.appspot.com/_ah/api/";

    /**
     * Endpoint root URL for testing setup.
     * TODO Replace with own IP for testing
     */
    private static final String TEST_ROOT_URL = "http://192.168.1.163:8080/_ah/api/";

    public static Endpoint.Users users() {
        return endpoint().users();
    }

    public static Endpoint.Parties parties() {
        return endpoint().parties();
    }

    public static Endpoint.Dishes dishes() {
        return endpoint().dishes();
    }

    public static Endpoint.Ingredients ingredients() {
        return endpoint().ingredients();
    }

    public static Endpoint.Fridge fridge() {
        return endpoint().fridge();
    }

    private static Endpoint endpoint() {
        Endpoint.Builder builder = new Endpoint.Builder(newHttpTransport(), getJsonFactory(), null);
        return prepare(builder).build();
    }

    private static HttpTransport newHttpTransport() {
        return AndroidHttp.newCompatibleTransport();
    }

    private static JsonFactory getJsonFactory() {
        return GsonFactory.getDefaultInstance();
    }

    private static <T extends AbstractGoogleJsonClient.Builder> T prepare(T builder) {
        builder.setApplicationName(Application.get().getString(R.string.app_name));
        if (TESTING) {
            builder.setRootUrl(TEST_ROOT_URL);
        } else {
            builder.setRootUrl(PRODUCTION_ROOT_URL);
        }
        return builder;
    }

}

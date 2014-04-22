package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.appengine.api.utils.SystemProperty;

/**
 * Created by Mattias on 22/04/2014.
 */
public class Application {

    private Application() {
    }

    /**
     * Check whether we're running on the local development server.
     */
    public static final boolean isDevelopment() {
        return SystemProperty.environment.value() == SystemProperty.Environment.Value.Development;
    }

}

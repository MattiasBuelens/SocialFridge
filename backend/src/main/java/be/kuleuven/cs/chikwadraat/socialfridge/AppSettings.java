package be.kuleuven.cs.chikwadraat.socialfridge;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Application settings.
 * Read from /resources/app.properties
 */
public final class AppSettings {

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("resources/app.properties"));
        } catch (IOException e) {
        }
    }

    private AppSettings() {
    }

    public static String getFacebookAppId() {
        return properties.getProperty("fb_app_id");
    }

    public static String getFacebookAppSecret() {
        return properties.getProperty("fb_app_secret");
    }

}

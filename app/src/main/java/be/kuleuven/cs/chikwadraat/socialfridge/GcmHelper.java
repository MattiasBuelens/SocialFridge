package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Mattias on 4/04/2014.
 */
public class GcmHelper {

    private static final String TAG = "GcmHelper";

    private static final String PREF_NAME = "gcm";
    private static final String PREF_REG_ID = "registration_id";
    private static final String PREF_APP_VERSION = "app_version";

    private final Context context;

    public GcmHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or null if there is no existing registration ID.
     */
    public String getRegistrationID() {
        final SharedPreferences prefs = getPreferences();
        String regID = prefs.getString(PREF_REG_ID, "");
        if (regID.isEmpty()) {
            Log.i(TAG, "Registration not found");
            return null;
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed, invalidate registration ID");
            return null;
        }
        return regID;
    }

    /**
     * Registers this device on GCM, if not yet registered.
     *
     * @return registration ID.
     * @throws IOException
     */
    public String register() throws IOException {
        String registrationID = getRegistrationID();
        if (registrationID == null) {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            registrationID = gcm.register(getSenderID());
            storeRegistrationID(registrationID);
        }
        return registrationID;
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regID registration ID
     */
    private void storeRegistrationID(String regID) {
        final SharedPreferences prefs = getPreferences();
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving registration ID on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PREF_REG_ID, regID);
        editor.putInt(PREF_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private String getSenderID() {
        return context.getString(R.string.google_sender_id);
    }

    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

}

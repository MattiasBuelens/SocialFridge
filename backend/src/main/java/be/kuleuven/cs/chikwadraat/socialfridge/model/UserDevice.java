package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.datanucleus.api.jpa.annotations.Extension;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Device of a registered user.
 */
@Entity(name = UserDevice.KIND)
public class UserDevice {

    public static final String KIND = "UserDevice";

    /**
     * Composite key with (User, registrationID).
     */
    @Id
    private Key key;

    /**
     * Parent key of user.
     */
    @Extension(vendorName = "datanucleus", key = "gae.parent-pk", value = "true")
    private Key userKey;

    private String information;
    private long timestamp;

    public UserDevice(Key userKey, String registrationID, String information, long timestamp) {
        this(getKey(userKey, registrationID), information, timestamp);
    }

    public UserDevice(String userID, String registrationID, String information, long timestamp) {
        this(getKey(userID, registrationID), information, timestamp);
    }

    private UserDevice(Key key, String information, long timestamp) {
        this.key = key;
        this.information = information;
        this.timestamp = timestamp;
    }

    public Key getKey() {
        return key;
    }

    public Key getUserKey() {
        return userKey;
    }

    public static Key getKey(String userID, String registrationID) {
        return getKey(User.getKey(userID), registrationID);
    }

    public static Key getKey(Key userKey, String registrationID) {
        return KeyFactory.createKey(userKey, KIND, registrationID);
    }

    public static Key getUserKey(Key deviceKey) {
        return deviceKey.getParent();
    }

    public static String getUserID(Key deviceKey) {
        return User.getID(getUserKey(deviceKey));
    }

    /**
     * The parent user ID.
     */
    public String getUserID() {
        return getUserKey().getName();
    }

    /*
     * The Google Cloud Messaging registration token for the device. This token
     * indicates that the device is able to receive messages sent via GCM.
     */
    public String getRegistrationID() {
        return getKey().getName();
    }

    /*
     * Some identifying information about the device, such as its manufacturer
     * and product name.
     */
    public String getInformation() {
        return this.information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    /*
     * Timestamp indicating when this device registered with the application.
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.datanucleus.api.jpa.annotations.Extension;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Device of a registered user.
 */
@Entity(name = UserDevice.KIND)
public class UserDevice {

    public static final String KIND = "UserDevice";

    @Id
    private Key key;

    /**
     * Registration ID.
     */
    @Extension(vendorName = "datanucleus", key = "gae.pk-name", value = "true")
    private String registrationID;

    /**
     * Parent user.
     */
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = User.class)
    private User user;

    /**
     * Key of parent user.
     */
    @Extension(vendorName = "datanucleus", key = "gae.parent-pk", value = "true")
    private Key userKey;

    private String information;
    private long timestamp;

    public UserDevice() {
    }

    public UserDevice(User user, String registrationID, String information, long timestamp) {
        this.key = getKey(user, registrationID);
        this.user = user;
        this.userKey = user.getKey();
        this.registrationID = registrationID;
        this.information = information;
        this.timestamp = timestamp;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key getKey() {
        return getKey(getUser(), getRegistrationID());
    }

    public static Key getKey(User user, String registrationID) {
        return getKey(user.getKey(), registrationID);
    }

    public static Key getKey(String userID, String registrationID) {
        return getKey(User.getKey(userID), registrationID);
    }

    public static Key getKey(Key userKey, String registrationID) {
        return KeyFactory.createKey(userKey, KIND, registrationID);
    }

    /**
     * The parent user.
     */
    public User getUser() {
        return user;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getUserID() {
        return getUser().getID();
    }

    /*
     * The Google Cloud Messaging registration token for the device. This token
     * indicates that the device is able to receive messages sent via GCM.
     */
    public String getRegistrationID() {
        return registrationID;
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

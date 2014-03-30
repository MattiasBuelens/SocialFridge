package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;


/**
 * Device of a registered user.
 */
@Entity(name = UserDevice.KIND)
public class UserDevice {

    public static final String KIND = "UserDevice";

    /**
     * Parent user.
     */
    @Parent
    private Ref<User> user;

    /**
     * Registration ID.
     */
    @Id
    private String registrationID;

    private String information;
    private long timestamp;

    public UserDevice() {
    }

    public UserDevice(User user, String registrationID, String information, long timestamp) {
        this.user = Ref.create(user);
        this.registrationID = registrationID;
        this.information = information;
        this.timestamp = timestamp;
    }

    /**
     * Parent user.
     */
    public User getUser() {
        return getUserRef().get();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<User> getUserRef() {
        return user;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getUserID() {
        return getUserRef().getKey().getName();
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

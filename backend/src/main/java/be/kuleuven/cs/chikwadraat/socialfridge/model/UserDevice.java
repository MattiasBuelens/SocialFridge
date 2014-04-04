package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;


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
     * Device identifier.
     */
    @Id
    private Long id;

    /**
     * Registration ID.
     */
    @Index
    private String registrationID;

    private Date timestamp;

    public UserDevice() {
    }

    public UserDevice(User user, String registrationID, Date timestamp) {
        this.user = Ref.create(user);
        this.registrationID = registrationID;
        this.timestamp = timestamp;
    }

    /**
     * Parent user.
     */
    public User getUser() {
        return getUserRef().get();
    }

    public void setUser(User user) {
        setUserRef(Ref.create(user));
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<User> getUserRef() {
        return user;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setUserRef(Ref<User> userRef) {
        this.user = userRef;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getUserID() {
        return getUserRef().getKey().getName();
    }

    /**
     * Device identifier.
     */
    public Long getID() {
        return id;
    }

    /*
     * The Google Cloud Messaging registration token for the device. This token
     * indicates that the device is able to receive messages sent via GCM.
     */
    public String getRegistrationID() {
        return registrationID;
    }

    public void setRegistrationID(String registrationID) {
        this.registrationID = registrationID;
    }

    /*
     * Timestamp indicating when this device registered with the application.
     */
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

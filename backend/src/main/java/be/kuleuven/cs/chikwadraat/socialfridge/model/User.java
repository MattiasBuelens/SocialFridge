package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 */
@Entity(name = User.KIND)
public class User {

    public static final String KIND = "User";

    @Id
    private String id;

    private String name;

    /**
     * Device registration IDs.
     */
    private Set<String> devices = new HashSet<String>();

    public User() {
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * User ID.
     */
    public String getID() {
        return id;
    }

    /**
     * User name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Device registration IDs.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<String> getDevices() {
        return devices;
    }

    public void addDevice(String registrationID) {
        devices.add(registrationID);
    }

    public void removeDevice(String registrationID) {
        devices.remove(registrationID);
    }

}

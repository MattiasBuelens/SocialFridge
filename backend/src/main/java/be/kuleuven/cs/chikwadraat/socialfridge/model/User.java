package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
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

    public static Key<User> getKey(String userID) {
        return Key.create(User.class, userID);
    }

    public static Ref<User> getRef(String userID) {
        return Ref.create(getKey(userID));
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
    public Set<String> getDevices() {
        return devices;
    }

    public void setDevices(Set<String> devices) {
        this.devices = devices;
    }

    public void addDevice(String device) {
        devices.add(device);
    }

    public void addDevices(Collection<String> newDevices) {
        this.devices.addAll(newDevices);
    }

    public void removeDevice(String device) {
        devices.remove(device);
    }

}

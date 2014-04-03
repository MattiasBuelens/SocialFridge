package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

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
     * Devices by registration IDs.
     */
    private Map<String, Ref<UserDevice>> devices = new HashMap<String, Ref<UserDevice>>();

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

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Set<String> getDeviceIDs() {
        return devices.keySet();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<Ref<UserDevice>> getDeviceKeys() {
        return devices.values();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<UserDevice> getDevices() {
        return ofy().load().refs(getDeviceKeys()).values();
    }

    public Ref<UserDevice> getDeviceKey(String registrationID) {
        return devices.get(registrationID);
    }

    public Ref<UserDevice> getDeviceKey(long deviceID) {
        for (Ref<UserDevice> ref : getDeviceKeys()) {
            if (deviceID == ref.getKey().getId()) {
                return ref;
            }
        }
        return null;
    }

    public UserDevice getDevice(String registrationID) {
        Ref<UserDevice> ref = getDeviceKey(registrationID);
        return ref == null ? null : ref.get();
    }

    public UserDevice getDevice(long deviceID) {
        Ref<UserDevice> ref = getDeviceKey(deviceID);
        return ref == null ? null : ref.get();
    }

    public void addDevice(UserDevice device) {
        devices.put(device.getRegistrationID(), Ref.create(device));
    }

    public void updateDevice(UserDevice device) {
        String regID = device.getRegistrationID();
        UserDevice existingDevice = getDevice(device.getID());
        if (existingDevice != null) {
            devices.remove(existingDevice.getRegistrationID());
        }
        devices.put(regID, Ref.create(device));
    }

    public void moveDevice(UserDevice device, String newRegID) {
        devices.remove(device.getRegistrationID());
        device.setRegistrationID(newRegID);
        devices.put(newRegID, Ref.create(device));
    }

    public void removeDevice(UserDevice device) {
        devices.remove(device.getRegistrationID());
    }

}

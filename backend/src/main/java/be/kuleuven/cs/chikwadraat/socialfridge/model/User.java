package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Collection;
import java.util.HashSet;
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
     * Device registration IDs.
     */
    private Set<String> devices = new HashSet<String>();

    /**
     * Parties.
     */
    private Set<Ref<Party>> parties = new HashSet<Ref<Party>>();

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

    /**
     * Parties.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<Party> getParties() {
        return ofy().load().refs(getPartyRefs()).values();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<Ref<Party>> getPartyRefs() {
        return parties;
    }

    public void addParty(Ref<Party> partyRef) {
        parties.add(partyRef);
    }

    public void addParty(Party party) {
        addParty(Ref.create(party));
    }

    public void removeParty(Ref<Party> partyRef) {
        parties.remove(partyRef);
    }

    public void removeParty(Party party) {
        removeParty(Ref.create(party));
    }

}

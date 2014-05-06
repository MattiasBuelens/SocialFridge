package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Serialize;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Message for user.
 */
@Entity(name = UserMessage.KIND)
public class UserMessage {

    public static final String KIND = "Message";

    /**
     * Message ID.
     */
    @Id
    private Long id;

    /**
     * User.
     */
    @Parent
    private Ref<User> user;

    /**
     * Receiving devices.
     */
    private Set<String> receivingDevices = new HashSet<String>();

    /**
     * Collapse key.
     */
    private String collapseKey;

    /**
     * Message data.
     */
    @Serialize
    private Map<String, String> data = new HashMap<String, String>();

    public UserMessage() {
    }

    public UserMessage(User user, String collapseKey, Map<String, String> data) {
        this.user = Ref.create(user);
        this.receivingDevices = new HashSet<String>(user.getDevices());
        this.collapseKey = collapseKey;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public static Key<UserMessage> getKey(String userID, long messageID) {
        return Key.create(User.getKey(userID), UserMessage.class, messageID);
    }

    public static Ref<UserMessage> getRef(String userID, long messageID) {
        return Ref.create(getKey(userID, messageID));
    }

    public Ref<User> getUserRef() {
        return user;
    }

    public Collection<String> getReceivingDevices() {
        return receivingDevices;
    }

    public void setReceivingDevices(Collection<String> receivingDevices) {
        this.receivingDevices.clear();
        this.receivingDevices.addAll(receivingDevices);
    }

    public void addReceivingDevice(String device) {
        this.receivingDevices.add(device);
    }

    public void removeReceivingDevice(String device) {
        this.receivingDevices.remove(device);
    }

    public String getCollapseKey() {
        return collapseKey;
    }

    protected void setCollapseKey(String collapseKey) {
        this.collapseKey = collapseKey;
    }

    public Map<String, String> getData() {
        return data;
    }

    protected void setData(Map<String, String> data) {
        this.data.clear();
        this.data.putAll(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserMessage message = (UserMessage) o;
        if (id != null ? !id.equals(message.id) : message.id != null) return false;
        if (!user.equals(message.user)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + user.hashCode();
        return result;
    }

}
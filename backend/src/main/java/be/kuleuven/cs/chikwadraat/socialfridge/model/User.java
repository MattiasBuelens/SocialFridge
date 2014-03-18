package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * User.
 */
@Entity(name = User.KIND)
public class User {

    public static final String KIND = "User";

    @Id
    private String id;

    private String name;

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

    public static Key getKey(User user) {
        return getKey(user.getID());
    }

    public static Key getKey(String id) {
        return KeyFactory.createKey(KIND, id);
    }

    public static String getID(Key userKey) {
        return userKey.getName();
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

}

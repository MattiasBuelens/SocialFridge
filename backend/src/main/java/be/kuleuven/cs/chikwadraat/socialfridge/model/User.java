package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
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

    public User() {
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key getKey() {
        return getKey(getID());
    }

    public static Key getKey(String id) {
        return KeyFactory.createKey(KIND, id);
    }

    /**
     * User ID.
     */
    public String getID() {
        return id;
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

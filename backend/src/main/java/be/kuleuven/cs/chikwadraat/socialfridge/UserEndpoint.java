package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import be.kuleuven.cs.chikwadraat.socialfridge.auth.FacebookAuthEndpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

@Api(
        name = "users",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserEndpoint extends FacebookAuthEndpoint {

    /**
     * Retrieves a user by user ID.
     *
     * @param id          The user ID.
     * @param accessToken The access token for authorization.
     * @return The retrieved user.
     */
    @ApiMethod(name = "getUser", path = "user/{id}")
    public User getUser(@Named("id") String id, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, id);
        EntityManager mgr = getEntityManager();
        User user = null;
        try {
            user = mgr.find(User.class, User.getKey(id));
        } finally {
            mgr.close();
        }
        return user;
    }

    /**
     * Inserts a user. If the user already
     * exists, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param user        The user to be inserted.
     * @param accessToken The access token for authorization.
     * @return The newly inserted user.
     */
    @ApiMethod(name = "insertUser", path = "user")
    public User insertUser(User user, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, user.getID());
        EntityManager mgr = getEntityManager();
        try {
            if (containsUser(mgr, user)) {
                throw new EntityExistsException("User already registered");
            }
            mgr.persist(user);
        } finally {
            mgr.close();
        }
        return user;
    }

    /**
     * Inserts or updates a user.
     * It uses HTTP PUT method.
     *
     * @param user        The user to be updated.
     * @param accessToken The access token for authorization.
     * @return The updated user.
     */
    @ApiMethod(name = "updateUser", path = "user")
    public User updateUser(User user, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, user.getID());
        EntityManager mgr = getEntityManager();
        try {
            mgr.persist(user);
        } finally {
            mgr.close();
        }
        return user;
    }

    /**
     * Removes a user.
     * It uses HTTP DELETE method.
     *
     * @param id          The user ID to be deleted.
     * @param accessToken The access token for authorization.
     * @return The deleted user.
     */
    @ApiMethod(name = "removeUser", path = "user/{id}")
    public User removeUserDevice(@Named("id") String id, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, id);
        EntityManager mgr = getEntityManager();
        User user = null;
        try {
            user = mgr.find(User.class, User.getKey(id));
            mgr.remove(user);
        } finally {
            mgr.close();
        }
        return user;
    }

    private boolean containsUser(EntityManager mgr, User user) {
        User item = mgr.find(User.class, user.getKey());
        return item != null;
    }

    private static EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

}

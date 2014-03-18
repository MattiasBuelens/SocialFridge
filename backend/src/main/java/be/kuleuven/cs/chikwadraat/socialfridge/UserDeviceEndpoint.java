package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.datastore.Key;

import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import be.kuleuven.cs.chikwadraat.socialfridge.auth.AuthException;
import be.kuleuven.cs.chikwadraat.socialfridge.auth.FacebookAuthEndpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserDevice;

@Api(name = "userDeviceEndpoint", namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge"))
public class UserDeviceEndpoint extends FacebookAuthEndpoint {

    /**
     * Retrieves a user device.
     *
     * @param key         The key of the user device.
     * @param accessToken The access token for authorization.
     * @return The retrieved user device.
     */
    @ApiMethod(name = "getUserDevice")
    public UserDevice getUserDevice(Key key, @Named("access_token") String accessToken) throws AuthException {
        checkAccess(accessToken, UserDevice.getUserID(key));
        EntityManager mgr = getEntityManager();
        UserDevice userDevice = null;
        try {
            userDevice = mgr.find(UserDevice.class, key);
        } finally {
            mgr.close();
        }
        return userDevice;
    }

    /**
     * Registers a user device. If the device is already
     * registered, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param userDevice  The user device to be registered.
     * @param accessToken The access token for authorization.
     * @return The newly registered user device.
     */
    @ApiMethod(name = "insertUserDevice")
    public UserDevice insertUserDevice(UserDevice userDevice, @Named("access_token") String accessToken) throws AuthException {
        checkAccess(accessToken, userDevice.getUserID());
        EntityManager mgr = getEntityManager();
        try {
            if (containsUserDevice(mgr, userDevice)) {
                throw new EntityExistsException("User device already registered");
            }
            mgr.persist(userDevice);
        } finally {
            mgr.close();
        }
        return userDevice;
    }

    /**
     * Updates a user device. If the device is not yet
     * registered, an exception is thrown.
     * It uses HTTP PUT method.
     *
     * @param userDevice  The user device to be updated.
     * @param accessToken The access token for authorization.
     * @return The updated user device.
     */
    @ApiMethod(name = "updateUserDevice")
    public UserDevice updateUserDevice(UserDevice userDevice, @Named("access_token") String accessToken) throws AuthException {
        checkAccess(accessToken, userDevice.getUserID());
        EntityManager mgr = getEntityManager();
        try {
            if (!containsUserDevice(mgr, userDevice)) {
                throw new EntityNotFoundException("User device not yet registered");
            }
            mgr.persist(userDevice);
        } finally {
            mgr.close();
        }
        return userDevice;
    }

    /**
     * Removes a user device.
     * It uses HTTP DELETE method.
     *
     * @param key         The key of the user device to be deleted.
     * @param accessToken The access token for authorization.
     * @return The deleted user device.
     */
    @ApiMethod(name = "removeUserDevice")
    public UserDevice removeUserDevice(Key key, @Named("access_token") String accessToken) throws AuthException {
        checkAccess(accessToken, UserDevice.getUserID(key));
        EntityManager mgr = getEntityManager();
        UserDevice userDevice = null;
        try {
            userDevice = mgr.find(UserDevice.class, key);
            mgr.remove(userDevice);
        } finally {
            mgr.close();
        }
        return userDevice;
    }

    private boolean containsUserDevice(EntityManager mgr, UserDevice userDevice) {
        UserDevice item = mgr.find(UserDevice.class, userDevice.getKey());
        return item != null;
    }

    private static EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

}

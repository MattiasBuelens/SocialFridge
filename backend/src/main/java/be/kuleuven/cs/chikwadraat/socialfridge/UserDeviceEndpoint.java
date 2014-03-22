package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;

import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;

import be.kuleuven.cs.chikwadraat.socialfridge.auth.FacebookAuthEndpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserDevice;

@Api(
        name = "userDevices",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserDeviceEndpoint extends FacebookAuthEndpoint {

    /**
     * Retrieves a user device.
     *
     * @param userID         The user ID of the device owner.
     * @param registrationID The registration ID of the device.
     * @param accessToken    The access token for authorization.
     * @return The retrieved user device.
     */
    @ApiMethod(name = "getUserDevice", path = "userDevice/{userID}/{registrationID}")
    public UserDevice getUserDevice(@Named("userID") String userID, @Named("registrationID") String registrationID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        EntityManager mgr = getEntityManager();
        UserDevice userDevice = null;
        try {
            userDevice = mgr.find(UserDevice.class, UserDevice.getKey(userID, registrationID));
        } finally {
            mgr.close();
        }
        return userDevice;
    }

    /**
     * Inserts a user device. If the device already
     * exists, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param userDevice  The user device to be registered.
     * @param accessToken The access token for authorization.
     * @return The newly registered user device.
     */
    @ApiMethod(name = "insertUserDevice", path = "userDevice")
    public UserDevice insertUserDevice(UserDevice userDevice, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userDevice.getUserID());
        EntityManager mgr = getEntityManager();
        try {
            if (containsUserDevice(mgr, userDevice)) {
                throw new ConflictException(new EntityExistsException("User device already registered"));
            }
            mgr.persist(userDevice);
        } finally {
            mgr.close();
        }
        return userDevice;
    }

    /**
     * Inserts or updates a user device.
     * It uses HTTP PUT method.
     *
     * @param userDevice  The user device to be updated.
     * @param accessToken The access token for authorization.
     * @return The updated user device.
     */
    @ApiMethod(name = "updateUserDevice", path = "userDevice")
    public UserDevice updateUserDevice(UserDevice userDevice, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userDevice.getUserID());
        EntityManager mgr = getEntityManager();
        try {
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
     * @param userID         The user ID of the device owner.
     * @param registrationID The registration ID of the device.
     * @param accessToken    The access token for authorization.
     * @return The deleted user device.
     */
    @ApiMethod(name = "removeUserDevice", path = "userDevice/{userID}/{registrationID}")
    public UserDevice removeUserDevice(@Named("userID") String userID, @Named("registrationID") String registrationID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        EntityManager mgr = getEntityManager();
        UserDevice userDevice = null;
        try {
            userDevice = mgr.find(UserDevice.class, UserDevice.getKey(userID, registrationID));
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

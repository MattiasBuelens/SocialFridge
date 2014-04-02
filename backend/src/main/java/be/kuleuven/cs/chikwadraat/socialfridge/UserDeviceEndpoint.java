package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.googlecode.objectify.Key;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserDevice;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Api(
        name = "users",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserDeviceEndpoint extends BaseEndpoint {

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
        return getUserDevice(userID, registrationID);
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
    public UserDevice insertUserDevice(final UserDevice userDevice, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userDevice.getUserID());
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                // Check if exists
                UserDevice existingDevice = ofy().load().entity(userDevice).now();
                if (existingDevice != null) {
                    throw new ConflictException("User device already registered");
                }
                // Save
                ofy().save().entity(userDevice).now();
                return userDevice;
            }
        });
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
        ofy().save().entity(userDevice).now();
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
    public UserDevice removeUserDevice(final @Named("userID") String userID, final @Named("registrationID") String registrationID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                UserDevice device = getUserDevice(userID, registrationID);
                ofy().delete().entity(device).now();
                return device;
            }
        });
    }

    private UserDevice getUserDevice(String userID, String registrationID) throws ServiceException {
        UserDevice device = ofy().load().type(UserDevice.class)
                .parent(Key.create(User.class, userID))
                .id(registrationID)
                .now();
        if (device == null) {
            throw new NotFoundException("User device not found.");
        }
        return device;
    }

}

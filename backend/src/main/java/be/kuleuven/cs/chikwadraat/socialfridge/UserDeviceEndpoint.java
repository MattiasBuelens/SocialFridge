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

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


@Api(
        name = "users",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserDeviceEndpoint extends BaseEndpoint {

    /**
     * Retrieves a user device.
     *
     * @param userID      The user ID of the device owner.
     * @param deviceID    The ID of the device.
     * @param accessToken The access token for authorization.
     * @return The retrieved user device.
     */
    @ApiMethod(name = "getUserDevice", path = "userDevice/{userID}/{deviceID}")
    public UserDevice getUserDevice(@Named("userID") String userID, @Named("deviceID") String deviceID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        return getUserDevice(userID, deviceID);
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
        return insertUserDevice(userDevice);
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
        return updateUserDevice(userDevice);
    }

    /**
     * Removes a user device.
     * It uses HTTP DELETE method.
     *
     * @param userID      The user ID of the device owner.
     * @param deviceID    The ID of the device.
     * @param accessToken The access token for authorization.
     * @return The deleted user device.
     */
    @ApiMethod(name = "removeUserDevice", path = "userDevice/{userID}/{registrationID}")
    public UserDevice removeUserDevice(final @Named("userID") String userID, final @Named("deviceID") String deviceID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        return removeUserDevice(userID, deviceID);
    }

    private UserDevice getUserDevice(String userID, String deviceID) throws ServiceException {
        UserDevice device = ofy().load().type(UserDevice.class)
                .parent(Key.create(User.class, userID))
                .id(deviceID)
                .now();
        if (device == null) {
            throw new NotFoundException("User device not found.");
        }
        return device;
    }

    private UserDevice insertUserDevice(final UserDevice userDevice) throws ServiceException {
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                // Check if exists
                UserDevice existingDevice = ofy().load().entity(userDevice).now();
                if (existingDevice != null) {
                    throw new ConflictException("User device already registered");
                }
                // Save device
                updateUserDevice(userDevice);
                return userDevice;
            }
        });
    }

    private UserDevice updateUserDevice(final UserDevice userDevice) throws ServiceException {
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                // Update user
                User user = userDevice.getUser();
                user.updateDevice(userDevice);
                // Save user and device
                ofy().save().entities(user, userDevice).now();
                return userDevice;
            }
        });
    }

    private UserDevice moveUserDevice(final UserDevice userDevice, final String newRegID) throws ServiceException {
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                // Update in parent user
                User user = userDevice.getUser();
                user.moveDevice(userDevice, newRegID);
                // Save user and device
                ofy().save().entities(user, userDevice).now();
                return userDevice;
            }
        });
    }

    private UserDevice removeUserDevice(final String userID, final String deviceID) throws ServiceException {
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                UserDevice userDevice = getUserDevice(userID, deviceID);
                return removeUserDevice(userDevice);
            }
        });
    }

    private UserDevice removeUserDevice(final UserDevice userDevice) throws ServiceException {
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                if (userDevice == null) {
                    return null;
                }
                // Delete device
                ofy().delete().entity(userDevice);
                // Update user
                User user = userDevice.getUser();
                user.removeDevice(userDevice);
                ofy().save().entity(user).now();
                return userDevice;
            }
        });
    }

}

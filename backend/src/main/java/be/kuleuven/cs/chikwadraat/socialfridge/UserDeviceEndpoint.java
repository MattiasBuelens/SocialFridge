package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.googlecode.objectify.Key;

import java.util.Date;

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
     * Inserts or updates a user device.
     * It uses HTTP PUT method.
     *
     * @param userID         The user ID of the device owner.
     * @param registrationID The registration ID of the device.
     * @param accessToken    The access token for authorization.
     * @return The updated user device.
     */
    @ApiMethod(name = "updateUserDevice", path = "userDevice/{userID}/{registrationID}")
    public void updateUserDevice(final @Named("userID") String userID, final @Named("registrationID") String registrationID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        registerUserDevice(userID, registrationID);
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
    public void removeUserDevice(final @Named("userID") String userID, final @Named("registrationID") String registrationID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        unregisterUserDevice(userID, registrationID);
    }

    private User getUser(String id) throws ServiceException {
        User user = ofy().load().type(User.class).id(id).now();
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        return user;
    }

    private UserDevice getUserDevice(Key<User> userKey, String registrationID) throws ServiceException {
        UserDevice device = ofy().load().type(UserDevice.class)
                .filter("registrationID", registrationID)
                .ancestor(userKey)
                .limit(1)
                .first()
                .now();
        if (device == null) {
            throw new NotFoundException("User device not found.");
        }
        return device;
    }

    private UserDevice getUserDevice(String userID, String registrationID) throws ServiceException {
        return getUserDevice(Key.create(User.class, userID), registrationID);
    }

    private UserDevice registerUserDevice(final String userID, final String registrationID) throws ServiceException {
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                UserDevice userDevice = getUserDevice(userID, registrationID);
                if (userDevice == null) {
                    // Insert device
                    User user = getUser(userID);
                    userDevice = new UserDevice(user, registrationID, new Date());
                    ofy().save().entity(userDevice).now();
                }
                // Update user
                User user = userDevice.getUser();
                user.updateDevice(userDevice);
                // Save user and device
                ofy().save().entities(user, userDevice).now();
                return userDevice;
            }
        });
    }

    private UserDevice unregisterUserDevice(final String userID, final String registrationID) throws ServiceException {
        return transact(new Work<UserDevice, ServiceException>() {
            @Override
            public UserDevice run() throws ServiceException {
                UserDevice userDevice = getUserDevice(userID, registrationID);
                if (userDevice == null) return null;
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

}

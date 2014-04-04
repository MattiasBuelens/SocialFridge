package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


@Api(
        name = "users",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserDeviceEndpoint extends BaseEndpoint {

    /**
     * Inserts or updates a user device.
     * It uses HTTP GET method. (POST/PUT throws EOF without content)
     *
     * @param userID         The user ID of the device owner.
     * @param registrationID The registration ID of the device.
     * @param accessToken    The access token for authorization.
     * @return The updated user device.
     */
    @ApiMethod(name = "updateUserDevice", path = "userDevice/{userID}/{registrationID}", httpMethod = ApiMethod.HttpMethod.GET)
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

    protected void registerUserDevice(final User user, final String registrationID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                user.addDevice(registrationID);
                ofy().save().entity(user).now();
            }
        });
    }

    protected void registerUserDevice(final String userID, final String registrationID) throws ServiceException {
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                registerUserDevice(getUser(userID), registrationID);
            }
        });
    }

    protected void unregisterUserDevice(final User user, final String registrationID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                user.removeDevice(registrationID);
                ofy().save().entity(user).now();
            }
        });
    }

    protected void unregisterUserDevice(final String userID, final String registrationID) throws ServiceException {
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                unregisterUserDevice(getUser(userID), registrationID);
            }
        });
    }

    protected void moveUserDevice(final User user, final String oldRegID, final String newRegID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                user.removeDevice(oldRegID);
                user.addDevice(newRegID);
                ofy().save().entity(user).now();
            }
        });
    }

    protected void moveUserDevice(final String userID, final String oldRegID, final String newRegID) throws ServiceException {
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                moveUserDevice(getUser(userID), oldRegID, newRegID);
            }
        });
    }

}

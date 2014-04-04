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
public class UserEndpoint extends BaseEndpoint {

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
        return getUser(id);
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
        ofy().save().entity(user).now();
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
    public User removeUser(final @Named("id") String id, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, id);
        return transact(new Work<User, ServiceException>() {
            @Override
            public User run() throws ServiceException {
                User user = getUser(id);
                ofy().delete().entity(user).now();
                return user;
            }
        });
    }

    private User getUser(String id) throws ServiceException {
        User user = ofy().load().type(User.class).id(id).now();
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        return user;
    }

}

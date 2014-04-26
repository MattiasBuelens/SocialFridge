package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;


@Api(
        name = "endpoint",
        version = "v2",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserEndpoint extends BaseEndpoint {

    private UserDAO dao = new UserDAO();

    /**
     * Retrieves a user by user ID.
     *
     * @param id          The user ID.
     * @param accessToken The access token for authorization.
     * @return The retrieved user.
     */
    @ApiMethod(name = "users.getUser", path = "user/{id}")
    public User getUser(@Named("id") String id, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, id);
        User user = User.getRef(id).get();
        if (user == null) {
            throw new NotFoundException("User not found.");
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
    @ApiMethod(name = "users.updateUser", path = "user")
    public User updateUser(final User user, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, user.getID());
        return dao.updateUser(user);
    }

    /**
     * Removes a user.
     * It uses HTTP DELETE method.
     *
     * @param id          The user ID to be deleted.
     * @param accessToken The access token for authorization.
     * @return The deleted user.
     */
    @ApiMethod(name = "users.removeUser", path = "user/{id}")
    public User removeUser(final @Named("id") String id, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, id);
        User user = dao.removeUser(User.getRef(id));
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        return user;
    }

}

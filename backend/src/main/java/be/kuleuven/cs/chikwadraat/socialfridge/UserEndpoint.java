package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;
import com.googlecode.objectify.Work;

import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static com.googlecode.objectify.ObjectifyService.ofy;

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
     * Inserts a user. If the user already
     * exists, an exception is thrown.
     * It uses HTTP POST method.
     *
     * @param user        The user to be inserted.
     * @param accessToken The access token for authorization.
     * @return The newly inserted user.
     */
    @ApiMethod(name = "insertUser", path = "user")
    public User insertUser(final User user, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, user.getID());
        try {
            return ofy().transact(new Work<User>() {
                @Override
                public User run() {
                    // Check if exists
                    User existingUser = ofy().load().entity(user).now();
                    if (existingUser != null) {
                        throw new EntityExistsException("User already registered");
                    }
                    // Save
                    ofy().save().entity(user).now();
                    return user;
                }
            });
        } catch (EntityExistsException e) {
            throw new ConflictException(e);
        }
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
        return ofy().transact(new Work<User>() {
            @Override
            public User run() {
                User user = getUser(id);
                ofy().delete().entity(user).now();
                return user;
            }
        });
    }

    private User getUser(String id) {
        User user = ofy().load().type(User.class).id(id).now();
        if(user == null) {
            throw new EntityNotFoundException("User not found.");
        }
        return user;
    }

}

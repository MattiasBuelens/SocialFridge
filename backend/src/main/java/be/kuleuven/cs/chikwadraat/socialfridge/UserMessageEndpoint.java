package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserMessage;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


@Api(
        name = "endpoint",
        version = "v3",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserMessageEndpoint extends BaseEndpoint {

    private final UserMessageDAO dao = new UserMessageDAO();

    /**
     * Removes a user's message.
     * It uses HTTP DELETE method.
     *
     * @param userID      The user ID owning the message.
     * @param messageID   The message ID to be deleted.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "users.removeMessage", path = "user/{userID}/message/{messageID}")
    public void removeMessage(final @Named("userID") String userID, final @Named("messageID") long messageID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        dao.removeMessage(userID, messageID);
    }


}

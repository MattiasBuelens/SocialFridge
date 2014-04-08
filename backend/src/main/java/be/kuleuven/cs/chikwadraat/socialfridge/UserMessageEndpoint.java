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
        name = "users",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class UserMessageEndpoint extends BaseEndpoint {

    /**
     * Adds user messages and queues them for sending.
     *
     * @param userMessages The new messages.
     */
    protected void addMessages(final Collection<UserMessage> userMessages) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                // Save messages
                Set<Key<UserMessage>> keys = ofy().save().entities(userMessages).now().keySet();
                // Add to queue
                Queue queue = QueueFactory.getQueue(MessageWorker.QUEUE);
                List<TaskOptions> options = new ArrayList<TaskOptions>();
                for (Key<UserMessage> key : keys) {
                    options.add(TaskOptions.Builder
                            .withTaskName(key.getString())
                            .param(MessageWorker.PARAM_MESSAGE_KEY, key.getString()));
                }
                queue.add(ofy().getTransaction(), options);
            }
        });
    }

    /**
     * Adds user messages and queues them for sending.
     *
     * @param userMessages The new messages.
     */
    protected void addMessages(final UserMessage... userMessages) {
        addMessages(Arrays.asList(userMessages));
    }

    /**
     * Updates a user's message.
     *
     * @param userMessage The updated message.
     */
    protected void updateMessage(final UserMessage userMessage) {
        ofy().save().entity(userMessage).now();
    }

    /**
     * Removes a user's message.
     *
     * @param userMessage The message to be deleted.
     */
    protected void removeMessage(final UserMessage userMessage) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                // Delete message
                Key<UserMessage> key = Key.create(userMessage);
                ofy().delete().entity(userMessage).now();

                // Delete task
                Queue queue = QueueFactory.getQueue(MessageWorker.QUEUE);
                queue.deleteTask(key.getString());
            }
        });
    }

    /**
     * Removes a user's message.
     *
     * @param userID    The user ID owning the message.
     * @param messageID The message ID to be deleted.
     */
    protected void removeMessage(final String userID, final long messageID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                UserMessage userMessage = getMessageUnsafe(userID, messageID);
                if (userMessage != null) {
                    removeMessage(userMessage);
                }
            }
        });
    }

    /**
     * Removes a user's message.
     * It uses HTTP DELETE method.
     *
     * @param userID      The user ID owning the message.
     * @param messageID   The message ID to be deleted.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "removeMessage", path = "user/{userID}/message/{messageID}")
    public void removeMessage(final @Named("userID") String userID, final @Named("messageID") long messageID, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userID);
        removeMessage(userID, messageID);
    }

    protected UserMessage getMessage(String userID, long messageID) throws ServiceException {
        UserMessage user = getMessageUnsafe(userID, messageID);
        if (user == null) {
            throw new NotFoundException("User not found.");
        }
        return user;
    }

    protected UserMessage getMessageUnsafe(String userID, long messageID) {
        return ofy().load().type(UserMessage.class)
                .parent(Key.create(User.class, userID))
                .id(messageID)
                .now();
    }

}

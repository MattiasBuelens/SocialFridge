package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.googlecode.objectify.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import be.kuleuven.cs.chikwadraat.socialfridge.model.UserMessage;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


public class UserMessageDAO {

    /**
     * Adds user messages and queues them for sending.
     *
     * @param userMessages The new messages.
     */
    public void addMessages(final Collection<UserMessage> userMessages) {
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
                            .withParam(MessageWorker.PARAM_MESSAGE_KEY, key.getString()));
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
    public void addMessages(final UserMessage... userMessages) {
        addMessages(Arrays.asList(userMessages));
    }

    /**
     * Retrieve a user's message.
     *
     * @param userMessageKey The user message's key.
     */
    public UserMessage getMessage(final Key<UserMessage> userMessageKey) {
        return ofy().load().key(userMessageKey).now();
    }

    /**
     * Retrieve a user's message.
     *
     * @param userID    The user ID owning the message.
     * @param messageID The message ID to be retrieved.
     */
    public UserMessage getMessage(final String userID, final long messageID) {
        return getMessage(UserMessage.getKey(userID, messageID));
    }

    /**
     * Updates a user's message.
     *
     * @param userMessage The updated message.
     */
    public void updateMessage(final UserMessage userMessage) {
        ofy().save().entity(userMessage).now();
    }

    /**
     * Removes a user's message.
     *
     * @param userMessage The message to be deleted.
     */
    public void removeMessage(final UserMessage userMessage) {
        ofy().delete().entity(userMessage).now();
    }

    /**
     * Removes a user's message.
     *
     * @param userMessage The message to be deleted.
     */
    public void removeMessage(final Key<UserMessage> userMessage) {
        ofy().delete().key(userMessage).now();
    }

    /**
     * Removes a user's message.
     *
     * @param userID    The user ID owning the message.
     * @param messageID The message ID to be deleted.
     */
    public void removeMessage(final String userID, final long messageID) {
        removeMessage(UserMessage.getKey(userID, messageID));
    }

}

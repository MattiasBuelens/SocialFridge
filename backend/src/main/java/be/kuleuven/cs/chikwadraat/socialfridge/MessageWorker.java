package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Work;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.kuleuven.cs.chikwadraat.socialfridge.model.UserMessage;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

/**
 * Worker for sending messages through GCM.
 * <p>
 * Original from Google Cloud Messaging samples.
 * https://code.google.com/p/gcm/source/browse/samples/gcm-demo-appengine/src/com/google/android/gcm/demo/server/SendMessageServlet.java
 * </p>
 */
public class MessageWorker extends HttpServlet {

    public static final String QUEUE = "messaging";
    public static final String PARAM_MESSAGE_KEY = "message_key";

    private static final int NUM_RETRIES = 5;

    private final Logger log = Logger.getLogger(MessageWorker.class.getName());

    private final UserDAO userDAO = new UserDAO();
    private final UserMessageDAO messageDAO = new UserMessageDAO();

    /**
     * Indicates to App Engine that this task should be retried.
     */
    private void retryTask(HttpServletResponse resp) {
        resp.setStatus(500);
    }

    /**
     * Indicates to App Engine that this task is done.
     */
    private void taskDone(HttpServletResponse resp) {
        resp.setStatus(200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Retrieve message
        final Key<UserMessage> messageKey = Key.create(req.getParameter(PARAM_MESSAGE_KEY));
        UserMessage message = messageDAO.getMessage(messageKey);
        if (message == null) {
            // Message already deleted
            taskDone(resp);
            return;
        }

        // Setup
        Sender sender = new Sender(AppSettings.getCloudMessagingApiKey());
        Message msg = new Message.Builder()
                .collapseKey(message.getCollapseKey())
                .setData(message.getData())
                .build();
        final List<String> regIDs = new ArrayList<String>(message.getReceivingDevices());

        // Send message
        final MulticastResult multicastResult;
        try {
            multicastResult = sender.send(msg, regIDs, NUM_RETRIES);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception sending " + msg, e);
            return;
        }

        // Handle send result
        boolean allDone = ofy().transact(new Work<Boolean>() {
            @Override
            public Boolean run() {
                return handleSendResult(messageKey, regIDs, multicastResult);
            }
        });

        // Check result
        if (allDone) {
            // Task done
            taskDone(resp);
        } else {
            // Retry
            retryTask(resp);
        }
    }

    private boolean handleSendResult(Key<UserMessage> messageKey, List<String> regIDs, MulticastResult multicastResult) {
        UserMessage message = messageDAO.getMessage(messageKey);
        List<Result> results = multicastResult.getResults();
        boolean allDone = true;

        // Check if any registration ID must be updated
        if (multicastResult.getCanonicalIds() != 0) {
            for (int i = 0; i < results.size(); i++) {
                Result result = results.get(i);
                String regID = regIDs.get(i);
                String canonicalRegID = result.getCanonicalRegistrationId();
                if (canonicalRegID != null) {
                    // If the registration ID changed, move the device
                    log.info("Registration ID changed for " + regID + " updating to " + canonicalRegID);
                    userDAO.moveUserDevice(message.getUserRef(), regID, canonicalRegID);
                }
            }
        }

        // Check for failures
        if (multicastResult.getFailure() != 0) {
            // Check if any could be retried
            List<String> retryRegIDs = new ArrayList<String>();
            for (int i = 0; i < results.size(); i++) {
                String error = results.get(i).getErrorCodeName();
                String regID = regIDs.get(i);
                if (error != null) {
                    log.warning("Got error (" + error + ") for regID " + regID);
                    if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                        // If the device is no longer registered with GCM, remove it
                        log.warning("Registration ID " + regID + " no longer registered with GCM, removing");
                        message.removeReceivingDevice(regID);
                        userDAO.unregisterUserDevice(message.getUserRef(), regID);
                    }
                    if (error.equals(Constants.ERROR_UNAVAILABLE)) {
                        // GCM is currently unavailable, retry later
                        retryRegIDs.add(regID);
                    }
                }
            }
            if (!retryRegIDs.isEmpty()) {
                // Update message with retryRegIDs
                message.setReceivingDevices(retryRegIDs);
                allDone = false;
            }
        }

        if (allDone) {
            // All done, remove
            messageDAO.removeMessage(message);
        } else {
            // Not done yet, update
            messageDAO.updateMessage(message);
        }

        return allDone;
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

/**
 * Created by Mattias on 3/04/2014.
 */
public class MessagingHelper {

    private static final Logger log = Logger.getLogger(MessagingHelper.class.getName());

    private static final int NUM_RETRIES = 5;

    /**
     * Sends a non-collapsible message to the given recipients.
     *
     * @param data       The message data.
     * @param recipients The message recipients.
     */
    public void sendMessage(Map<String, String> data, List<User> recipients) {
        sendMessage(null, data, recipients);
    }

    /**
     * Sends a message to the given recipients.
     *
     * @param collapseKey The collapse key for the message.
     * @param data        The message data.
     * @param recipients  The message recipients.
     */
    public void sendMessage(String collapseKey, Map<String, String> data, List<User> recipients) {
        Sender sender = new Sender(AppSettings.getCloudMessagingApiKey());
        Message msg = new Message.Builder()
                .collapseKey(collapseKey)
                .setData(data)
                .build();

        Map<String, User> userRegIDs = new HashMap<String, User>();
        for (User user : recipients) {
            for (String regID : user.getDevices()) {
                userRegIDs.put(regID, user);
            }
        }
        List<String> regIDs = new ArrayList<String>(userRegIDs.keySet());

        MulticastResult multicastResult;
        try {
            multicastResult = sender.send(msg, regIDs, NUM_RETRIES);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Exception sending " + msg, e);
            return;
        }
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
                    User user = userRegIDs.get(regID);
                    log.info("Registration ID changed for " + regID + " updating to " + canonicalRegID);
                    new UserDeviceEndpoint().moveUserDevice(user, regID, canonicalRegID);
                }
            }
        }
        // Check for failures
        if (multicastResult.getFailure() != 0) {
            // Check if any could be retried
            List<String> retryRegIDs = new ArrayList<String>();
            for (int i = 0; i < results.size(); i++) {
                String error = results.get(i).getErrorCodeName();
                if (error != null) {
                    String regID = regIDs.get(i);
                    log.warning("Got error (" + error + ") for regId " + regID);
                    if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                        // If the device is no longer registered with GCM, remove it
                        log.warning("Registration Id " + regID + " no longer registered with GCM, removing");
                        User user = userRegIDs.get(regID);
                        new UserDeviceEndpoint().unregisterUserDevice(user, regID);
                    }
                    if (error.equals(Constants.ERROR_UNAVAILABLE)) {
                        // GCM is currently unavailable, retry later
                        retryRegIDs.add(regID);
                    }
                }
            }
            if (!retryRegIDs.isEmpty()) {
                // TODO Update task in datastore with retryRegIDs
                //Datastore.updateMulticast(encodedKey, retryRegIDs);
                allDone = false;
                //retryTask(resp);
            }
        }
        if (allDone) {
            // TODO Remove task from datastore
            //multicastDone(resp, encodedKey);
        } else {
            //retryTask(resp);
        }
    }

}

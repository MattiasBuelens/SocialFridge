package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

/**
 * Created by Mattias on 3/04/2014.
 */
public class Messaging {

    private static final Logger log = Logger.getLogger(Messaging.class.getName());

    private static final int NUM_RETRIES = 5;

    /**
     * Sends a message to the given recipients.
     *
     * @param collapseKey The collapse key for the message.
     * @param data        The message data.
     * @param recipients  The message recipients.
     */
    public void sendMessage(String collapseKey, Map<String, String> data, List<User> recipients) throws IOException {
        Sender sender = new Sender(AppSettings.getCloudMessagingApiKey());
        Message msg = new Message.Builder()
                .collapseKey(collapseKey)
                .setData(data)
                .build();

        for (User user : recipients) {
            for (String regID : user.getDevices()) {
                sendMessage(sender, msg, user, regID);
            }
        }
    }

    protected void sendMessage(Sender sender, Message msg, User user, String regID) throws IOException {
        Result result = sender.send(msg, regID, NUM_RETRIES);
        if (result.getMessageId() != null) {
            log.fine("Message sent to " + regID);
            String canonicalRegID = result.getCanonicalRegistrationId();
            if (canonicalRegID != null) {
                // If the regId changed, move the device
                log.info("Registration ID changed for " + regID + " updating to " + canonicalRegID);
                new UserDeviceEndpoint().moveUserDevice(user, regID, canonicalRegID);
            }
        } else {
            String error = result.getErrorCodeName();
            if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                log.warning("Registration Id " + regID + " no longer registered with GCM, removing");
                // If the device is no longer registered with GCM, remove it
                new UserDeviceEndpoint().unregisterUserDevice(user, regID);
            } else {
                log.warning("Error when sending message: " + error);
            }
        }
    }

}

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
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserDevice;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

/**
 * Created by Mattias on 3/04/2014.
 */
public class Messaging {

    private static final Logger log = Logger.getLogger(Messaging.class.getName());

    private static final int NUM_RETRIES = 5;

    /**
     * Send to the first 10 devices (You can modify this to send to any number of devices or a specific device)
     *
     * @param collapseKey The collapse key for the message.
     * @param data        The message data.
     * @param recipients  The message recipients.
     */
    public void sendMessage(String collapseKey, Map<String, String> data, List<UserDevice> recipients) throws IOException {
        Sender sender = new Sender(AppSettings.getCloudMessagingApiKey());
        Message msg = new Message.Builder()
                .collapseKey(collapseKey)
                .setData(data)
                .build();
        for (UserDevice device : recipients) {
            String regID = device.getRegistrationID();
            Result result = sender.send(msg, regID, NUM_RETRIES);
            if (result.getMessageId() != null) {
                log.fine("Message sent to " + regID);
                String canonicalRegID = result.getCanonicalRegistrationId();
                if (canonicalRegID != null) {
                    // If the regId changed, move the device
                    log.info("Registration ID changed for " + regID + " updating to " + canonicalRegID);
                    // TODO Use UserDeviceEndpoint.moveUserDevice
                    User user = device.getUser();
                    user.moveDevice(device, canonicalRegID);
                    ofy().save().entities(user, device).now();
                }
            } else {
                String error = result.getErrorCodeName();
                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
                    log.warning("Registration Id " + regID + " no longer registered with GCM, removing");
                    // If the device is no longer registered with GCM, remove it
                    // TODO Use UserDeviceEndpoint.removeUserDevice
                    ofy().delete().entity(device);
                    User user = device.getUser();
                    user.removeDevice(device);
                    ofy().save().entity(user).now();
                } else {
                    log.warning("Error when sending message: " + error);
                }
            }
        }
    }

}

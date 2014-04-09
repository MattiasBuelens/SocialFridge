package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants;
import be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageType;
import be.kuleuven.cs.chikwadraat.socialfridge.notifications.NotificationConstants;
import be.kuleuven.cs.chikwadraat.socialfridge.notifications.NotificationService;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    private static final String TAG = "GcmIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.w(TAG, "Send error: " + extras.toString());
                // TODO
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.w(TAG, "Deleted messages on server: " + extras.toString());
                // TODO
                //sendNotification("Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Handle received message.
                handleMessage(extras);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleMessage(Bundle data) {
        MessageType type = MessageType.byName(data.getString(MessageConstants.ARG_TYPE));
        if (type == null) return;

        Log.d(TAG, "Received: " + type.getName());
        switch (type) {
            case PARTY_UPDATE:
                long partyID = data.getLong(MessageConstants.ARG_PARTY_ID);
                // TODO: ISSUE 4
                break;
            case PARTY_INVITE:
                // notify user
                Intent notificationIntent = new Intent(this, NotificationService.class);
                notificationIntent.setAction(NotificationConstants.ACTION_RECEIVE_INVITE);
                // simply add received bundle (from the message) to notificationIntent
                notificationIntent.putExtras(data);
                startService(notificationIntent);
                break;
            case PARTY_CANCEL_INVITE:
                // TODO: OMG YOU CAN'T DO THIS TO ME!
                break;
        }
    }

}


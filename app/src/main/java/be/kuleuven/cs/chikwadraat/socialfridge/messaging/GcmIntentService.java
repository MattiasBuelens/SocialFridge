package be.kuleuven.cs.chikwadraat.socialfridge.messaging;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseIntentService;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.PartyLoaderService;
import be.kuleuven.cs.chikwadraat.socialfridge.notifications.NotificationConstants;
import be.kuleuven.cs.chikwadraat.socialfridge.notifications.NotificationIntentService;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends BaseIntentService {

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
                handleMessage(new GcmMessage(extras));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void handleMessage(GcmMessage message) {
        if (message.getType() == null) return;

        Log.d(TAG, "Received: " + message.getType().getName());

        Intent notificationIntent = new Intent(this, NotificationIntentService.class);

        switch (message.getType()) {
            case PARTY_UPDATE:
                long partyID = message.getPartyID();
                // Notify user about party update
                notificationIntent.setAction(NotificationConstants.ACTION_PARTY_UPDATE);
                notificationIntent.putExtra(NotificationConstants.EXTRA_MESSAGE, message);
                startService(notificationIntent);
                // Reload party
                PartyLoaderService.startReload(this, partyID);
                break;
            case PARTY_INVITE:
                // Notify user about party invite
                notificationIntent.setAction(NotificationConstants.ACTION_RECEIVE_INVITE);
                notificationIntent.putExtra(NotificationConstants.EXTRA_MESSAGE, message);
                startService(notificationIntent);
                break;
            case PARTY_CANCEL_INVITE:
                // TODO: OMG YOU CAN'T DO THIS TO ME!
                break;
        }
    }

}


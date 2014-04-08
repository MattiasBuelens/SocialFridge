package be.kuleuven.cs.chikwadraat.socialfridge.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants;

/**
 * Created by vital.dhaveloose on 29/03/2014.
 *
 * Service for creating and updating notifications (from intents) as well as
 * responding to their actions.
 */
public class NotificationService extends IntentService {

    private NotificationManager nm;

    public NotificationService() {
        super(NotificationConstants.NOTIFICATION_ADDRESS);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String message = intent.getStringExtra(NotificationConstants.EXTRA_MESSAGE);

        String action = intent.getAction();
        // this section handles three different actions:
        // receive invite, accept and decline
        if (action.equals(NotificationConstants.ACTION_RECEIVE_INVITE)) {
            issueNotification(intent, message);
        } else if (action.equals(NotificationConstants.ACTION_CHOOSE_SLOTS)) {
            nm.cancel(NotificationConstants.NOTIFICATION_ID);
            // TODO: send accept message
        } else if (action.equals(NotificationConstants.ACTION_DECLINE)) {
            nm.cancel(NotificationConstants.NOTIFICATION_ID);
            // TODO: send decline message
        }
    }

    private void issueNotification(Intent intent, String msg) {
        // Sets up the Choose slots and Decline action buttons that will appear in the
        // expanded view of the notification.
        Intent chooseSlotsIntent = new Intent(this, NotificationService.class);
        chooseSlotsIntent.setAction(NotificationConstants.ACTION_CHOOSE_SLOTS);
        PendingIntent piChooseSlots = PendingIntent.getService(this, 0, chooseSlotsIntent, 0);

        Intent declineIntent = new Intent(this, NotificationService.class);
        declineIntent.setAction(NotificationConstants.ACTION_DECLINE);
        PendingIntent piDecline = PendingIntent.getService(this, 0, declineIntent, 0);

        String contextAndBigText = intent.getBundleExtra(MessageConstants.ARG_HOST_USER_NAME) + NotificationConstants.CONTENT_TEXT_POSTFIX;

        // Constructs the Builder object.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.stat_notify_chat) //TODO: klein icoontje instellen (fotootje van gerecht?)
                        .setContentTitle(NotificationConstants.CONTENT_TITLE)
                        .setContentText(contextAndBigText)
                        .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
                /*
                 * Sets the big view "big text" style and supplies the
                 * text (the user's reminder message) that will be displayed
                 * in the detail area of the expanded notification.
                 * These calls are ignored by the support library for
                 * pre-4.1 devices.
                 */
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(contextAndBigText)) //TODO: voorlopig contextText en bigText identiek
                        .addAction(0,   //TODO: choose slots icoontje invoegen
                                NotificationConstants.BUTTON_CHOOSE_SLOTS, piChooseSlots)
                        .addAction(0,   //TODO: decline icoontje invoegen
                                NotificationConstants.BUTTON_DECLINE, piDecline);

        /*
         * Clicking the notification itself displays ChooseSlotsActivity, which provides
         * UI for choosing time slots or declining the notification.
         * This is available through either the normal view or big view.
         */
        Intent resultIntent = new Intent(this, ChooseSlotsActivity.class);
        resultIntent.putExtra(NotificationConstants.EXTRA_MESSAGE, msg);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);

        nm.notify(NotificationConstants.NOTIFICATION_ID, builder.build());
    }

}

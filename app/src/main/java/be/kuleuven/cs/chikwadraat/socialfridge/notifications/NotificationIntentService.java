package be.kuleuven.cs.chikwadraat.socialfridge.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants;
import be.kuleuven.cs.chikwadraat.socialfridge.party.InviteReplyActivity;

/**
 * Created by vital.dhaveloose on 29/03/2014.
 * <p>
 * Service for creating and updating notifications (from intents) as well as
 * responding to their actions.
 * </p>
 */
public class NotificationIntentService extends IntentService {

    private NotificationManager nm;

    public NotificationIntentService() {
        super(NotificationConstants.NOTIFICATION_ADDRESS);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent receivedIntent) {
        String action = receivedIntent.getAction();
        // this section handles three different actions:
        // action 1: receive invite: receivedIntent comes from elsewhere (the GcmIntentService)
        if (action.equals(NotificationConstants.ACTION_RECEIVE_INVITE)) {
            issueNotification(receivedIntent);
            // action 2: choose slots: receivedIntent comes from this object: it's an
            // indirection to make sure the notification is deleted before proceeding
        } else if (action.equals(NotificationConstants.ACTION_CHOOSE_SLOTS)) {
            nm.cancel(NotificationConstants.NOTIFICATION_ID);
            startActivity(makeReplyIntent(receivedIntent.getExtras()));
            // action 3: decline: receivedIntent comes from this object: it's an
            // indirection to make sure the notification is deleted before proceeding
        } else if (action.equals(NotificationConstants.ACTION_DECLINE)) {
            nm.cancel(NotificationConstants.NOTIFICATION_ID);
            // TODO: send decline message
        }
    }

    private void issueNotification(Intent receivedIntent) {
        // Sets up the Choose slots and Decline action buttons that will appear in the
        // expanded view of the notification.
        PendingIntent piChooseSlots = makeActionIntent(NotificationConstants.ACTION_CHOOSE_SLOTS, receivedIntent.getExtras());
        PendingIntent piDecline = makeActionIntent(NotificationConstants.ACTION_DECLINE, receivedIntent.getExtras());

        String hostName = receivedIntent.getStringExtra(MessageConstants.ARG_HOST_USER_NAME);
        String contentTitle = getString(R.string.notif_party_invite_title);
        String contentText = getString(R.string.notif_party_invite_content, hostName, "spaghetti");

        // Constructs the Builder object.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(android.R.drawable.stat_notify_chat) //TODO: klein icoontje instellen (fotootje van gerecht/host?)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                        .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
                /*
                 * Sets the big view "big text" style and supplies the
                 * text (the user's reminder message) that will be displayed
                 * in the detail area of the expanded notification.
                 * These calls are ignored by the support library for
                 * pre-4.1 devices.
                 */
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(contentText)) //TODO: voorlopig contentText en bigText identiek
                        .addAction(0,   //TODO: choose slots icoontje invoegen
                                getString(R.string.notif_action_choose_slots), piChooseSlots)
                        .addAction(0,   //TODO: decline icoontje invoegen
                                getString(R.string.notif_action_decline), piDecline);

        /*
         * Clicking the notification itself displays InviteReplyActivity, which provides
         * UI for choosing time slots or declining the notification.
         * This is available through either the normal view or big view.
         */
        Intent resultIntent = makeReplyIntent(receivedIntent.getExtras());

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        // TODO Can't we just use piChooseSlots instead?
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        builder.setContentIntent(resultPendingIntent);
        builder.setDeleteIntent(piDecline);

        nm.notify(NotificationConstants.NOTIFICATION_ID, builder.build());
    }

    private PendingIntent makeActionIntent(String action, Bundle extras) {
        Intent intent = new Intent(this, NotificationIntentService.class);
        // Set the action
        intent.setAction(action);
        // Use the same extras
        intent.putExtras(extras);
        return PendingIntent.getService(this, 0, intent, 0);
    }

    private Intent makeReplyIntent(Bundle extras) {
        Intent intent = new Intent(this, InviteReplyActivity.class);
        // TODO: stupid translation, why not simply use the same language? Does the language have to be defined by the communication channel (i.e. MessageConstants)?
        intent.putExtra(InviteReplyActivity.EXTRA_PARTY_ID, extras.getLong(MessageConstants.ARG_PARTY_ID));
        // TODO Flags correct?
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

}

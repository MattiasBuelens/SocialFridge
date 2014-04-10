package be.kuleuven.cs.chikwadraat.socialfridge.notifications;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.messaging.GcmMessage;
import be.kuleuven.cs.chikwadraat.socialfridge.messaging.PartyUpdateReason;
import be.kuleuven.cs.chikwadraat.socialfridge.party.InviteReplyActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.party.ViewPartyActivity;

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
        if (action == null) return;

        GcmMessage message = receivedIntent.getParcelableExtra(NotificationConstants.EXTRA_MESSAGE);
        if (action.equals(NotificationConstants.ACTION_RECEIVE_INVITE)) {
            // Received party invite from GCM
            // Show notification
            issueInviteNotification(message);
        } else if (action.equals(NotificationConstants.ACTION_CHOOSE_SLOTS)) {
            // Choose slots action on party invite notification
            // Cancel notification first
            nm.cancel(NotificationConstants.NOTIFICATION_ID);
            // Open reply activity
            startActivity(makeReplyIntent(message));
        } else if (action.equals(NotificationConstants.ACTION_DECLINE)) {
            // Decline action on party invite notification
            // Cancel notification first
            nm.cancel(NotificationConstants.NOTIFICATION_ID);
            // TODO Decline invite
        } else if (action.equals(NotificationConstants.ACTION_PARTY_UPDATE)) {
            // Received party invite from GCM
            // Show notification if necessary
            issueUpdateNotification(message);
        } else if (action.equals(NotificationConstants.ACTION_VIEW_PARTY)) {
            startActivity(makeViewIntent(message));
        }
    }

    private void issueInviteNotification(GcmMessage message) {
        // Sets up the Choose slots and Decline action buttons that will appear in the
        // expanded view of the notification.
        PendingIntent piChooseSlots = makeActionIntent(NotificationConstants.ACTION_CHOOSE_SLOTS, message);
        PendingIntent piDecline = makeActionIntent(NotificationConstants.ACTION_DECLINE, message);

        String hostName = message.getHostUserName();
        String contentTitle = getString(R.string.notif_party_invite_title);
        String contentText = getString(R.string.notif_party_invite_content, hostName, "dinner");

        // Constructs the Builder object.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_fridge)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                                //.setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS)
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
         * Clicking the notification itself acts the same
         * as the Choose slots action.
         */
        builder.setContentIntent(piChooseSlots);

        nm.notify(NotificationConstants.NOTIFICATION_ID, builder.build());
    }

    private void issueUpdateNotification(GcmMessage message) {
        PartyUpdateReason reason = message.getUpdateReason();
        String contentTitle;
        String contentText;

        if (reason.equals(PartyUpdateReason.JOINED)) {
            String partnerName = message.getUpdateReasonUserName();
            contentTitle = getString(R.string.notif_party_joined_title);
            contentText = getString(R.string.notif_party_joined_content, partnerName, "dinner");
        } else if (reason.equals(PartyUpdateReason.DONE)) {
            // TODO Host shouldn't be notified, need to check this
            String hostName = message.getHostUserName();
            contentTitle = getString(R.string.notif_party_done_title);
            contentText = getString(R.string.notif_party_done_content, hostName, "dinner");
        } else {
            // Unknown reason
            return;
        }

        // Constructs the Builder object.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_fridge)
                        .setContentTitle(contentTitle)
                        .setContentText(contentText)
                                //.setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS);

        PendingIntent piViewParty = makeActionIntent(NotificationConstants.ACTION_VIEW_PARTY, message);

        builder.setContentIntent(piViewParty);

        nm.notify(NotificationConstants.NOTIFICATION_ID, builder.build());
    }

    private PendingIntent makeActionIntent(String action, GcmMessage message) {
        Intent intent = new Intent(this, NotificationIntentService.class);
        // Set the action
        intent.setAction(action);
        // Use the same message
        intent.putExtra(NotificationConstants.EXTRA_MESSAGE, message);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Intent makeReplyIntent(GcmMessage message) {
        Intent intent = new Intent(this, InviteReplyActivity.class);
        intent.putExtra(InviteReplyActivity.EXTRA_PARTY_ID, message.getPartyID());
        // TODO Flags correct?
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private Intent makeViewIntent(GcmMessage message) {
        Intent intent = new Intent(this, ViewPartyActivity.class);
        intent.putExtra(ViewPartyActivity.EXTRA_PARTY_ID, message.getPartyID());
        // TODO Flags correct?
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.notifications;

/**
 * Created by vital.dhaveloose on 29/03/2014.
 *
 * Constants for describing actions etc.
 */
public abstract class NotificationConstants {

    // addresses and is's
    public static final String NOTIFICATION_ADDRESS = "be.kuleuven.cs.chikwadraat.socialfridge.notifications";
    public static final String ACTION_RECEIVE_INVITE = NOTIFICATION_ADDRESS + ".ACTION_RECEIVE_INVITE";
    public static final String ACTION_CHOOSE_SLOTS = NOTIFICATION_ADDRESS + ".ACTION_CHOOSE_SLOTS";
    public static final String ACTION_DECLINE = NOTIFICATION_ADDRESS + ".ACTION_DECLINE";
    public static final String ACTION_PARTY_UPDATE = NOTIFICATION_ADDRESS + ".ACTION_PARTY_UPDATE";

    public static final int NOTIFICATION_PARTY_INVITE = 1;
    public static final int NOTIFICATION_PARTY_UPDATE = 2;

    public static final String EXTRA_MESSAGE = NOTIFICATION_ADDRESS + ".EXTRA_MESSAGE";

}

package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.messaging.MessageConstants;
import be.kuleuven.cs.chikwadraat.socialfridge.notifications.NotificationConstants;
import be.kuleuven.cs.chikwadraat.socialfridge.notifications.NotificationService;
import be.kuleuven.cs.chikwadraat.socialfridge.party.BasePartyActivity;

/**
 * Created by vital.dhaveloose on 29/03/2014.
 *
 * This Activity is displayed when users click the notification itself. It provides
 * UI for choosing time slots or as yet declining the invitation.
 */
public class InviteReplyActivity extends BasePartyActivity {

    private static final String TAG = "InviteReplyActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_party);
        //TODO: verder afhandelen: knoppen koppelen etc.
    }

    public void onReady(View v) {
        //TODO: interpreteer geselecteerde slots, stuur bericht terug, sluit view af
    }

}

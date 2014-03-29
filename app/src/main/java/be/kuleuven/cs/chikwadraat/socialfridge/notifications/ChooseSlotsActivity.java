package be.kuleuven.cs.chikwadraat.socialfridge.notifications;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;

/**
 * Created by vital.dhaveloose on 29/03/2014.
 *
 * This Activity is displayed when users click the notification itself. It provides
 * UI for choosing time slots or as yet declining the invitation.
 */
public class ChooseSlotsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(null); //TODO: hier een view in steken...

    }

    public void onReady(View v) {
        //TODO: interpreteer geselecteerde slots, stuur bericht terug, sluit view af
    }

    public void onDecline(View v) {
        Intent intent = new Intent(getApplicationContext(), NotificationService.class);
        intent.setAction(NotificationConstants.ACTION_DECLINE);
        startService(intent);
    }

}

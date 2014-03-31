package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Time slot activity.
 * Choose time slots.
 */
public class TimeSlotActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_timeslots);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time_action_find_partners:


        }
    }

}



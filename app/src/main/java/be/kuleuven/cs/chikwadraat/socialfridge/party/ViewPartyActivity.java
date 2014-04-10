package be.kuleuven.cs.chikwadraat.socialfridge.party;

import android.os.Bundle;

import be.kuleuven.cs.chikwadraat.socialfridge.R;

/**
 * Activity to view a party.
 */
public class ViewPartyActivity extends BasePartyActivity {

    private static final String TAG = "ViewPartyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.party_view);
    }

}

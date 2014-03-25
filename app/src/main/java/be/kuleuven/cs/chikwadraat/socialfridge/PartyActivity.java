package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Party activity.
 *
 * Invite partners
 */
public class PartyActivity extends BaseActivity {

    private static final String TAG = "PartyActivity";

    @Override
    protected void onAfterCreate(Bundle savedInstanceState) {
        setContentView(R.layout.party);
    }
}

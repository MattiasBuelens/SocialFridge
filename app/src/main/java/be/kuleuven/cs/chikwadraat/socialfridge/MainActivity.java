package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.analytics.HitBuilders;

import be.kuleuven.cs.chikwadraat.socialfridge.dish.DishesActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.fridge.FridgeActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.party.CreatePartyActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.party.PartiesActivity;

/**
 * Main activity.
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private ProfilePictureView userPictureView;
    private TextView userNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);

        userPictureView = (ProfilePictureView) findViewById(R.id.current_user_pic);
        userPictureView.setCropped(true);
        userNameView = (TextView) findViewById(R.id.current_user_name);

        findViewById(R.id.action_create_party).setOnClickListener(this);
        findViewById(R.id.action_list_parties).setOnClickListener(this);
        findViewById(R.id.action_list_dishes).setOnClickListener(this);
        findViewById(R.id.action_manage_fridge).setOnClickListener(this);

        if (savedInstanceState != null) {
            // TODO Initialize stuff
        }
    }

    @Override
    protected boolean allowUpNavigation() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                getTracker().send(new HitBuilders.EventBuilder("User", "Logout").build());
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_create_party: {
                Intent intent = new Intent(this, DishesActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.action_list_parties: {
                Intent intent = new Intent(this, PartiesActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.action_manage_fridge: {
                Intent intent = new Intent(this, FridgeActivity.class);
                startActivity(intent);
            }
            break;
        }
    }

    /**
     * Updates the user's name and picture display.
     *
     * @param user The user.
     */
    private void updateUserProfile(User user) {
        userPictureView.setProfileId(user.getId());
        userNameView.setText(user.getName());
    }

    /**
     * Clears the user's name and picture display.
     */
    private void clearUserProfile() {
        userPictureView.setProfileId(null);
        userNameView.setText("");
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        updateUserProfile(user);
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        clearUserProfile();
    }

}

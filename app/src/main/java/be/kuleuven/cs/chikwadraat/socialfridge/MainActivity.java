package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.users.Users;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

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

        if (savedInstanceState != null) {
            // TODO Initialize stuff
        }
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
                logout();
                return true;
        }
        return false;
    }

    /**
     * Registers the user.
     *
     * @param graphUser The Facebook user.
     * @param session   The user session.
     */
    private void registerUser(GraphUser graphUser, Session session) {
        // Create user
        User user = new User();
        user.setId(graphUser.getId());
        user.setName(graphUser.getName());
        // Send register request
        new RegisterUserTask(this, session).execute(user);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_create_party:
                //Intent intent = new Intent(this, PartyActivity.class);
                Intent intent = new Intent(this, TimeSlotActivity.class);
                startActivity(intent);
                break;
        }
    }

    private static class RegisterUserTask extends AsyncTask<User, Void, User> {

        private final Context context;
        private final Session session;
        private Exception exception;

        private RegisterUserTask(Context context, Session session) {
            this.context = context.getApplicationContext();
            this.session = session;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected User doInBackground(User... users) {
            User user = users[0];

            Users.Builder builder = new Users.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
            Users endpoint = Endpoints.prepare(builder, context).build();

            try {
                return endpoint.updateUser(session.getAccessToken(), user).execute();
            } catch (IOException e) {
                exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                Log.d(TAG, "User successfully registered");
            } else if (exception != null) {
                // TODO Error handling
                Log.e(TAG, "Failed to register user: " + exception.getMessage());
            }
        }
    }

    /**
     * Updates the user's name and picture display.
     *
     * @param graphUser The Facebook user.
     */
    private void updateUserProfile(GraphUser graphUser) {
        userPictureView.setProfileId(graphUser.getId());
        userNameView.setText(graphUser.getName());
    }

    /**
     * Clears the user's name and picture display.
     */
    private void clearUserProfile() {
        userPictureView.setProfileId(null);
        userNameView.setText("");
    }

    private void requestUserProfile(final Session session) {
        newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (user != null) {
                    userProfileReceived(session, user);
                }
                if (response.getError() != null) {
                    handleError(response.getError());
                }
            }
        }).executeAsync();
    }

    private void userProfileReceived(Session session, GraphUser user) {
        registerUser(user, session);
        updateUserProfile(user);
    }

    @Override
    protected void onLoggedIn(Session session) {
        super.onLoggedIn(session);
        requestUserProfile(session);
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        clearUserProfile();
    }

}

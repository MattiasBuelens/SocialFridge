package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.users.Users;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Start screen fragment.
 */
public class StartFragment extends Fragment {

    private static final String TAG = "StartFragment";

    private ProfilePictureView userPictureView;
    private TextView userNameView;
    private MainActivity activity;

    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        uiHelper = new UiLifecycleHelper(getActivity(), sessionCallback);
        uiHelper.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.start, container, false);

        userPictureView = (ProfilePictureView) view.findViewById(R.id.current_user_pic);
        userPictureView.setCropped(true);
        userNameView = (TextView) view.findViewById(R.id.current_user_name);

        init(savedInstanceState);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
        // TODO Write stuff
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
        activity = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.start, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                activity.showSettingsFragment();
                return true;
            case R.id.action_logout:
                activity.logout();
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
        new RegisterUserTask(session).execute(user);
    }

    private static class RegisterUserTask extends AsyncTask<User, Void, User> {

        private final Session session;
        private Exception exception;

        private RegisterUserTask(Session session) {
            this.session = session;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected User doInBackground(User... users) {
            User user = users[0];
            Users endpoint = new Users.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null)
                    //.setRootUrl("http://192.168.0.100:8080/_ah/api/") // TODO uncomment and replace with own IP for testing
                    .build();
            try {
                return endpoint.insertUser(session.getAccessToken(), user).execute();
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

    /**
     * Triggered when the session is opened.
     */
    private void sessionOpened(Session session) {
        requestUserProfile(session);
    }

    /**
     * Triggered when the user profile is received.
     */
    private void userProfileReceived(Session session, GraphUser user) {
        registerUser(user, session);
        updateUserProfile(user);
    }

    /**
     * Triggered when session token is updated.
     */
    private void sessionTokenUpdated(Session session) {
        // TODO Report to backend?
    }

    /**
     * Triggered when the session is closed.
     */
    private void sessionClosed() {
        clearUserProfile();
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                sessionTokenUpdated(session);
            } else {
                sessionOpened(session);
            }
        } else {
            sessionClosed();
        }
    }

    private void requestUserProfile(final Session session) {
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        userProfileReceived(session, user);
                    }
                    if (response.getError() != null) {
                        handleError(response.getError());
                    }
                }
            }
        }).executeAsync();
    }

    /**
     * Resets the view to the initial defaults.
     */
    private void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // TODO Initialize stuff
        }

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            sessionOpened(session);
        }
    }

    public void handleError(FacebookRequestError error) {
        if (error == null) return;
        if (activity == null) return;
        activity.handleError(error);
    }

}

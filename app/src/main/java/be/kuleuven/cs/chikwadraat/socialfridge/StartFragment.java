package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

/**
 * Start screen fragment.
 */
public class StartFragment extends Fragment {

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
     * Updates the user's name and picture display.
     *
     * @param session The user session.
     */
    private void updateUserProfile(final Session session) {
        Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        userPictureView.setProfileId(user.getId());
                        userNameView.setText(user.getName());
                    }
                }
                if (response.getError() != null) {
                    activity.handleError(response.getError());
                }
            }
        }).executeAsync();
    }

    /**
     * Clears the user's name and picture display.
     */
    private void clearUserProfile() {
        userPictureView.setProfileId(null);
        userNameView.setText("");
    }

    /**
     * Notifies that the session token has been updated.
     */
    private void sessionTokenUpdated() {
        // TODO Report to backend?
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                sessionTokenUpdated();
            } else {
                updateUserProfile(session);
            }
        } else {
            clearUserProfile();
        }
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
            updateUserProfile(session);
        }
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.messaging.GcmHelper;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Users;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Login activity.
 */
public class LoginActivity extends BaseActivity implements ObservableAsyncTask.Listener<LoginActivity.RegisterUserState, User> {

    private static final String TAG = "LoginActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private RegisterUserTask task;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        // Request public profile and friends list
        loginButton.setReadPermissions("basic_info");

        // Re-attach to registration task
        task = (RegisterUserTask) getLastCustomNonConfigurationInstance();
        if (task != null) {
            task.attach(this);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                setResult(RESULT_CANCELED);
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected boolean requiresLogin() {
        return false;
    }

    @Override
    protected void onFacebookLoggedIn(Session session) {
        super.onFacebookLoggedIn(session);

        // Logged in on Facebook, register
        registerUser(session);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);

        // Logged in on our app, done
        onResult(user);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (task != null) {
            task.detach();
        }
        return task;
    }

    /**
     * Registers the user.
     *
     * @param session The user session.
     */
    protected void registerUser(Session session) {
        if (!checkPlayServices()) return;

        if (task == null) {
            task = new RegisterUserTask(this, session);
            task.execute();
        }
    }

    private void removeRegisterTask() {
        if (task != null) {
            task.detach();
            task = null;
        }
    }

    @Override
    public void onResult(User user) {
        Log.d(TAG, "User successfully registered");
        removeRegisterTask();
        hideProgressDialog();

        // Registered, finish
        setLoggedInUser(user);
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to register user: " + exception.getMessage());
        removeRegisterTask();
        hideProgressDialog();
        logout();

        if (exception instanceof FacebookRequestException) {
            // Handle Facebook error
            handleError(((FacebookRequestException) exception).getError());
        } else {
            // Handle regular exception
            new AlertDialog.Builder(this)
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(R.string.error_dialog_title)
                    .setMessage(exception.getMessage())
                    .show();
        }
    }

    @Override
    public void onProgress(RegisterUserState... states) {
        RegisterUserState state = states[0];
        Log.d(TAG, "Registration progress: " + state);

        String progressMessage = getString(R.string.login_progress, getString(R.string.app_name));
        showProgressDialog(progressMessage);
    }

    protected static class RegisterUserTask extends ObservableAsyncTask<Void, RegisterUserState, User> {

        private final Context context;
        private final Session session;

        protected RegisterUserTask(LoginActivity activity, Session session) {
            super(activity);
            this.context = activity.getApplicationContext();
            this.session = session;
        }

        @Override
        protected User run(Void... unused) throws Exception {
            // Retrieve user information
            postProgress(RegisterUserState.RETRIEVE_INFO);
            User user = retrieveUserInfo();
            if (user == null) return null;

            // Register on GCM
            postProgress(RegisterUserState.REGISTER_GCM);
            user = registerGCM(user);

            // Register user
            postProgress(RegisterUserState.REGISTER_USER);
            user = registerUser(user);

            // Success
            return user;
        }

        private User retrieveUserInfo() throws FacebookRequestException {
            // Retrieve user info
            Response response = Request.newMeRequest(session, null).executeAndWait();
            if (response.getError() != null) {
                throw new FacebookRequestException(response.getError());
            }

            // Create user
            GraphUser graphUser = response.getGraphObjectAs(GraphUser.class);
            User user = new User();
            user.setId(graphUser.getId());
            user.setName(graphUser.getName());
            return user;
        }

        private User registerGCM(User user) throws IOException {
            // Register on GCM
            String registrationID = new GcmHelper(context).register();
            // Add registration ID to user's devices
            List<String> devices = user.getDevices();
            if (devices == null) {
                devices = new ArrayList<String>();
                user.setDevices(devices);
            }
            devices.add(registrationID);
            return user;
        }

        private User registerUser(User user) throws IOException {
            return users().updateUser(session.getAccessToken(), user).execute();
        }

        private Users users() {
            return Endpoints.users(context);
        }

    }

    public static enum RegisterUserState {
        RETRIEVE_INFO,
        REGISTER_USER,
        REGISTER_GCM
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.users.Users;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.widget.ProgressDialogFragment;

/**
 * Login activity.
 */
public class LoginActivity extends BaseActivity {

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
            task.attachActivity(this);
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
        onRegisterSuccess(user);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        task.detachActivity();
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
            task.detachActivity();
            task = null;
        }
    }

    protected void onRegisterSuccess(User user) {
        Log.d(TAG, "User successfully registered");
        removeRegisterTask();

        // Registered, finish
        setLoggedInUser(user);
        setResult(RESULT_OK);
        finish();
    }

    protected void onRegisterError(FacebookRequestError error) {
        Log.e(TAG, "Failed to register user: " + error.getErrorMessage());
        removeRegisterTask();

        handleError(error);
    }

    protected void onRegisterFailed(Exception exception) {
        Log.e(TAG, "Failed to register user: " + exception.getMessage());
        removeRegisterTask();

        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.error_dialog_title)
                .setMessage(exception.getMessage())
                .show();
    }

    protected void onRegisterUpdateProgress(RegisterUserState state) {
        Log.d(TAG, "Registration progress: " + state);

        if (state.showProgress()) {
            showProgressDialog();
        } else {
            hideProgressDialog();
        }
    }

    private void showProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment current = getSupportFragmentManager().findFragmentByTag("dialog");
        ProgressDialogFragment fragment;
        if (current != null) {
            fragment = (ProgressDialogFragment) current;
        } else {
            fragment = ProgressDialogFragment.newInstance(null);
            ft.add(fragment, "dialog");
            ft.addToBackStack(null);
        }
        String progressMessage = getString(R.string.login_progress, getString(R.string.app_name));
        fragment.setMessage(progressMessage);
        ft.show(fragment);
        ft.commit();
    }

    private void hideProgressDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment current = getSupportFragmentManager().findFragmentByTag("dialog");
        if (current != null) {
            ft.hide(current);
        }
        ft.commit();
    }

    protected static class RegisterUserTask extends AsyncTask<Void, RegisterUserState, Void> {

        private final Context context;
        private final Session session;
        private LoginActivity activity;

        private User user;
        private FacebookRequestError error;
        private Exception exception;
        private RegisterUserState state;

        protected RegisterUserTask(LoginActivity activity, Session session) {
            this.context = activity.getApplicationContext();
            this.session = session;
            this.state = RegisterUserState.WAITING;
            attachActivity(activity);
        }

        protected void attachActivity(LoginActivity activity) {
            this.activity = activity;
            switch (state) {
                case SUCCESS:
                    activity.onRegisterSuccess(user);
                    break;
                case FAILED:
                    if (error != null) {
                        activity.onRegisterError(error);
                    } else {
                        activity.onRegisterFailed(exception);
                    }
                    break;
                default:
                    activity.onRegisterUpdateProgress(state);
            }
        }

        protected void detachActivity() {
            this.activity = null;
        }

        private void setState(RegisterUserState state) {
            this.state = state;
            publishProgress(state);
        }

        private void setResult(User user) {
            this.user = user;
            setState(RegisterUserState.SUCCESS);
        }

        private void setError(FacebookRequestError e) {
            error = e;
            setState(RegisterUserState.FAILED);
        }

        private void setException(Exception e) {
            exception = e;
            setState(RegisterUserState.FAILED);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... unused) {
            try {
                // Retrieve user information
                setState(RegisterUserState.RETRIEVE_INFO);
                User user = retrieveUserInfo();
                if (user == null) return null;

                // Register on GCM
                setState(RegisterUserState.REGISTER_GCM);
                user = registerGCM(user);

                // Register user
                setState(RegisterUserState.REGISTER_USER);
                user = registerUser(user);

                // Success
                setResult(user);
            } catch (IOException e) {
                setException(e);
            }
            return null;
        }

        private User retrieveUserInfo() {
            // Retrieve user info
            Response response = Request.newMeRequest(session, null).executeAndWait();
            if (response.getError() != null) {
                setError(response.getError());
                return null;
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
            if(devices == null) {
                devices = new ArrayList<String>();
                user.setDevices(devices);
            }
            devices.add(registrationID);
            return user;
        }

        private User registerUser(User user) throws IOException {
            return getUsersEndpoint().updateUser(session.getAccessToken(), user).execute();
        }

        private Users getUsersEndpoint() {
            Users.Builder builder = new Users.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
            return Endpoints.prepare(builder, context).build();
        }

        @Override
        protected void onProgressUpdate(RegisterUserState... states) {
            RegisterUserState state = states[0];
            if (activity == null) {
                Log.d(TAG, "onProgressUpdate() skipped -- no activity");
            } else {
                activity.onRegisterUpdateProgress(state);
            }
        }

        @Override
        protected void onPostExecute(Void unused) {
            if (activity == null) {
                Log.d(TAG, "onPostExecute() skipped -- no activity");
            } else if (user != null) {
                activity.onRegisterSuccess(user);
            } else if (error != null) {
                activity.onRegisterError(error);
            } else if (exception != null) {
                activity.onRegisterFailed(exception);
            }
        }
    }

    public static enum RegisterUserState {
        WAITING(false),
        RETRIEVE_INFO(true),
        REGISTER_USER(true),
        REGISTER_GCM(true),
        SUCCESS(false),
        FAILED(false);

        private boolean showProgress;

        private RegisterUserState(boolean showProgress) {
            this.showProgress = showProgress;
        }

        public boolean showProgress() {
            return showProgress;
        }
    }

}

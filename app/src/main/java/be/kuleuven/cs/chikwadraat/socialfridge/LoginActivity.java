package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.users.Users;
import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Login fragment.
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

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

    @Override
    protected boolean requiresLogin() {
        return false;
    }

    @Override
    protected void onFacebookLoggedIn(Session session) {
        // Logged in on Facebook, register
        registerUser(session);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        // Logged in on our app, done
        onRegisterSuccess(user);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        task.detach();
        return task;
    }

    /**
     * Registers the user.
     *
     * @param session The user session.
     */
    protected void registerUser(Session session) {
        if (task == null) {
            task = new RegisterUserTask(this, session);
            task.execute();
        }
    }

    private void onRegisterSuccess(User user) {
        Log.d(TAG, "User successfully registered");
        task = null;

        // Registered, finish
        setResult(RESULT_OK);
        finish();
    }

    private void onRegisterError(FacebookRequestError error) {
        Log.e(TAG, "Failed to register user: " + error.getErrorMessage());
        task = null;

        handleError(error);
    }

    private void onRegisterFailed(Exception exception) {
        Log.e(TAG, "Failed to register user: " + exception.getMessage());
        task = null;

        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.error_dialog_title)
                .setMessage(exception.getMessage())
                .show();
    }

    private void onRegisterUpdateProgress(RegisterUserState state) {
        Log.d(TAG, "Registration progress: " + state);
        // TODO Show progress
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
            attach(activity);
        }

        protected void attach(LoginActivity activity) {
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

        protected void detach() {
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
            // Retrieve user information
            setState(RegisterUserState.LOGIN);
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

            // Register user
            setState(RegisterUserState.REGISTER);
            Users.Builder builder = new Users.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null);
            Users endpoint = Endpoints.prepare(builder, context).build();

            try {
                user = endpoint.updateUser(session.getAccessToken(), user).execute();
            } catch (IOException e) {
                setException(e);
                return null;
            }

            // Success
            setResult(user);
            return null;
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
        LOGIN,
        REGISTER,
        SUCCESS,
        FAILED
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import be.kuleuven.cs.chikwadraat.socialfridge.users.model.User;

/**
 * Base activity handling common things such as authentication.
 */
public abstract class BaseActivity extends ActionBarActivity {

    public static final int REQUEST_LOGIN = RESULT_FIRST_USER + 1;

    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    private AppSession appSession;

    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, sessionCallback);
        uiHelper.onCreate(savedInstanceState);

        appSession = new AppSession(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        appSession.onResume();
        isResumed = true;

        checkLoggedIn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
        appSession.onPause();
        isResumed = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOGIN) {
            if (isLoggedIn()) {
                checkLoggedIn();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    /**
     * Check whether this activity requires the user to be logged in.
     */
    protected boolean requiresLogin() {
        return true;
    }

    /**
     * Check if logged in.
     */
    protected boolean isLoggedIn() {
        return isFacebookLoggedIn() && appSession.isActive();
    }

    /**
     * Check if logged in on Facebook.
     */
    protected boolean isFacebookLoggedIn() {
        Session session = Session.getActiveSession();
        return session != null && session.isOpened();
    }

    /**
     * Log in through the {@link LoginActivity}.
     */
    public void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    /**
     * Set the logged in user.
     * Used by the {@link LoginActivity}.
     *
     * @param user The logged in user.
     */
    protected void setLoggedInUser(User user) {
        appSession.setUser(user);
    }

    /**
     * Log out.
     */
    public void logout() {
        appSession.clear();
        Session session = Session.getActiveSession();
        if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    private void checkLoggedIn() {
        if (isFacebookLoggedIn()) {
            if (isLoggedIn()) {
                onLoggedIn(Session.getActiveSession(), appSession.getUser());
            } else {
                onFacebookLoggedIn(Session.getActiveSession());
            }
        } else {
            onLoggedOut();
        }
    }

    /**
     * Called when user is logged in on our app.
     *
     * @param session The Facebook session.
     * @param user    The logged in user.
     */
    protected void onLoggedIn(Session session, User user) {

    }

    /**
     * Called when user is logged in on Facebook.
     * If this activity {@link #requiresLogin() requires login}, {@link #login()} is called.
     *
     * @param session The Facebook session.
     */
    protected void onFacebookLoggedIn(Session session) {
        // Clear session
        appSession.clear();
        // Retry login
        if (requiresLogin()) login();
    }

    /**
     * Called when user is logged out.
     * If this activity {@link #requiresLogin() requires login}, {@link #login()} is called.
     */
    protected void onLoggedOut() {
        // Clear session
        appSession.clear();
        // Retry login
        if (requiresLogin()) login();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the start fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
                onFacebookLoggedIn(session);
            } else if (state.isClosed()) {
                onLoggedOut();
            }
        }
    }

    /**
     * Handles errors from Facebook sessions and requests.
     *
     * @param error The error.
     */
    public void handleError(FacebookRequestError error) {
        if (error == null) return;

        DialogInterface.OnClickListener listener = null;
        String dialogBody;

        switch (error.getCategory()) {
            case AUTHENTICATION_RETRY:
                // tell the user what happened by getting the message id, and
                // retry the operation later
                String userAction = (error.shouldNotifyUser()) ? "" :
                        getString(error.getUserActionMessageId());
                dialogBody = getString(R.string.error_authentication_retry, userAction);
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, M_FACEBOOK_URL);
                        startActivity(intent);
                    }
                };
                break;

            case AUTHENTICATION_REOPEN_SESSION:
                // close the session and reopen it.
                dialogBody = getString(R.string.error_authentication_reopen);
                listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Session session = Session.getActiveSession();
                        if (session != null && !session.isClosed()) {
                            session.closeAndClearTokenInformation();
                        }
                    }
                };
                break;

            case PERMISSION:
                // request the publish permission
//                dialogBody = getString(R.string.error_permission);
//                listener = new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        pendingAnnounce = true;
//                        requestPublishPermissions(Session.getActiveSession());
//                    }
//                };
//                break;

            case SERVER:
            case THROTTLING:
                // this is usually temporary, don't clear the fields, and
                // ask the user to try again
                dialogBody = getString(R.string.error_server);
                break;

            case BAD_REQUEST:
                // this is likely a coding error, ask the user to file a bug
                dialogBody = getString(R.string.error_bad_request, error.getErrorMessage());
                break;

            case OTHER:
            case CLIENT:
            default:
                // an unknown issue occurred, this could be a code error, or
                // a server side issue, log the issue, and either ask the
                // user to retry, or file a bug
                dialogBody = getString(R.string.error_unknown, error.getErrorMessage());
                break;
        }

        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, listener)
                .setTitle(R.string.error_dialog_title)
                .setMessage(dialogBody)
                .show();
    }

}

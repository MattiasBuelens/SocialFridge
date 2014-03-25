package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

/**
 * Base activity handling common things such as authentication.
 */
public abstract class BaseActivity extends Activity {

    public static final int REQUEST_LOGIN = RESULT_FIRST_USER + 1;

    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, sessionCallback);
        uiHelper.onCreate(savedInstanceState);

        onAfterCreate(savedInstanceState);

        checkLoggedIn();
    }

    protected abstract void onAfterCreate(Bundle savedInstanceState);

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;

        checkLoggedIn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOGIN) {
            checkLoggedIn();
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
        Session session = Session.getActiveSession();
        return session != null && session.isOpened();
    }

    /**
     * Log in through the {@link be.kuleuven.cs.chikwadraat.socialfridge.LoginActivity}.
     */
    public void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, REQUEST_LOGIN);
    }

    /**
     * Log out.
     */
    public void logout() {
        Session session = Session.getActiveSession();
        if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    protected void checkLoggedIn() {
        if (isLoggedIn()) {
            onLoggedIn(Session.getActiveSession());
        } else {
            onLoggedOut();
        }
    }

    /**
     * Called when user is logged in.
     *
     * @param session
     */
    protected void onLoggedIn(Session session) {

    }

    /**
     * Called when user is logged out.
     * If this activity {@link #requiresLogin() requires login}, {@link #login()} is called.
     */
    protected void onLoggedOut() {
        if (requiresLogin()) login();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the start fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
                onLoggedIn(session);
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

package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

/**
 * Main activity.
 */
public class MainActivity extends FragmentActivity {

    private static final int LOGIN = 0;
    private static final int START = 1;
    private static final int DISHES = 2;
    private static final int FRAGMENT_COUNT = DISHES + 1;

    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback sessionCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            // TODO Initialize stuff
        }
        uiHelper = new UiLifecycleHelper(this, sessionCallback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        FragmentManager fm = getSupportFragmentManager();
        fragments[LOGIN] = fm.findFragmentById(R.id.fragment_login);
        fragments[START] = fm.findFragmentById(R.id.fragment_start);
        fragments[DISHES] = fm.findFragmentById(R.id.fragment_dishes);

        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);

        // TODO Write stuff
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open, try to show the selection fragment
            showFragment(START, false);
        } else {
            // otherwise present the login screen and ask the user to login
            showFragment(LOGIN, false);
        }
    }

    public void showSettingsFragment() {
        // TODO Show settings fragment
    }

    public void showDishesFragment() {
        showFragment(DISHES, true);
    }

    public void logout() {
        Session session = Session.getActiveSession();
        if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the start fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
                showFragment(START, false);
            } else if (state.isClosed()) {
                showFragment(LOGIN, false);
            }
        }
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * Handles errors from sessions and requests.
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
                    /*dialogBody = getString(R.string.error_permission);
                    listener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pendingAnnounce = true;
                            requestPublishPermissions(Session.getActiveSession());
                        }
                    };
                    break;*/

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

package be.kuleuven.cs.chikwadraat.socialfridge;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.facebook.FacebookRequestError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.facebook.FacebookRequestException;
import be.kuleuven.cs.chikwadraat.socialfridge.widget.ProgressDialogFragment;

/**
 * Base activity handling common things such as authentication.
 */
public abstract class BaseActivity extends ActionBarActivity {

    public static final int REQUEST_LOGIN = RESULT_FIRST_USER + 1;

    private static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

    private AppSession appSession;

    private boolean isResumed = false;
    private boolean isStarted = false;
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(allowUpNavigation());
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

    protected final boolean isActivityResumed() {
        return isResumed;
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

    @Override
    protected void onStart() {
        super.onStart();
        isStarted = true;
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStarted = false;
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    protected final boolean isActivityStarted() {
        return isStarted;
    }

    protected boolean allowUpNavigation() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                                    // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public Tracker getTracker() {
        return Application.get().getTracker();
    }

    public void trackException(Exception e) {
        Application.get().trackException(e);
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
        Session session = getSession();
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
     * Get the logged in user.
     *
     * @return The logged in user.
     */
    protected User getLoggedInUser() {
        return appSession.getUser();
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
        Session session = getSession();
        if (session != null && !session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    protected Session getSession() {
        return Session.getActiveSession();
    }

    private void checkLoggedIn() {
        if (isFacebookLoggedIn()) {
            if (isLoggedIn()) {
                onLoggedIn(getSession(), getLoggedInUser());
            } else {
                onFacebookLoggedIn(getSession());
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
        if (isActivityResumed()) {
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
     * Handles exceptions.
     *
     * @param exception The exception.
     */
    public void handleException(Exception exception) {
        String message = exception.getMessage();
        if (exception instanceof FacebookRequestException) {
            handleFacebookError(((FacebookRequestException) exception).getError());
            return;
        } else if (exception instanceof GoogleJsonResponseException) {
            GoogleJsonResponseException jsonException = (GoogleJsonResponseException) exception;
            if (jsonException.getDetails() != null) {
                message = jsonException.getDetails().getMessage();
            }
        }

        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.error_dialog_title)
                .setMessage(message)
                .show();
    }

    /**
     * Handles errors from Facebook sessions and requests.
     *
     * @param error The error.
     */
    private void handleFacebookError(FacebookRequestError error) {
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
                        Session session = getSession();
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

    protected void showProgressDialog(int messageResID) {
        showProgressDialog(getString(messageResID));
    }

    protected void showProgressDialog(String message) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment current = getSupportFragmentManager().findFragmentByTag("dialog");
        ProgressDialogFragment fragment;
        if (current != null) {
            fragment = (ProgressDialogFragment) current;
        } else {
            fragment = ProgressDialogFragment.newInstance();
            fragment.setCancelable(false);
            ft.add(fragment, "dialog");
            ft.addToBackStack(null);
        }
        fragment.setMessage(message);
        ft.show(fragment);
        ft.commitAllowingStateLoss();
    }

    protected void hideProgressDialog() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragment != null) {
            ((ProgressDialogFragment) fragment).dismissAllowingStateLoss();
        }
    }

}

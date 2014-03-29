package be.kuleuven.cs.chikwadraat.socialfridge;

import android.os.Bundle;

import com.facebook.Session;
import com.facebook.widget.LoginButton;

/**
 * Login fragment.
 */
public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";

    @Override
    protected void onAfterCreate(Bundle savedInstanceState) {
        setContentView(R.layout.login);

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        // Request public profile and friends list
        loginButton.setReadPermissions("basic_info");
    }

    @Override
    protected boolean requiresLogin() {
        return false;
    }

    @Override
    protected void onLoggedIn(Session session) {
        // Logged in, finish
        setResult(RESULT_OK);
        finish();
    }

}

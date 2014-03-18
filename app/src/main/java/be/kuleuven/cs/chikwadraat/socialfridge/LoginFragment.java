package be.kuleuven.cs.chikwadraat.socialfridge;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.widget.LoginButton;

/**
 * Login fragment.
 */
public class LoginFragment extends Fragment {

    private LoginButton loginButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.login, container, false);

        loginButton = (LoginButton) view.findViewById(R.id.login_button);

        init(savedInstanceState);

        return view;
    }

    private void init(Bundle savedInstanceState) {
        // Request public profile and friends list
        loginButton.setReadPermissions("basic_info");
    }

}

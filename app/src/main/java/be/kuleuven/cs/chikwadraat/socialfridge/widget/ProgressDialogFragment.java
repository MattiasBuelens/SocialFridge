package be.kuleuven.cs.chikwadraat.socialfridge.widget;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import be.kuleuven.cs.chikwadraat.socialfridge.R;

/**
 * Created by Mattias on 1/04/2014.
 */
public class ProgressDialogFragment extends DialogFragment {

    private static final String SAVED_MESSAGE = "message";

    private String message;

    public static ProgressDialogFragment newInstance() {
        return newInstance(null);
    }

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setMessage(message);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            message = savedInstanceState.getString(SAVED_MESSAGE);
        }
    }

    @Override
    public ProgressDialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getMessage());
        dialog.setIndeterminate(true);
        return dialog;
    }

    @Override
    public ProgressDialog getDialog() {
        return (ProgressDialog) super.getDialog();
    }

    public String getMessage() {
        if (message != null) {
            return message;
        } else if (getActivity() != null) {
            return getActivity().getString(R.string.loading);
        } else {
            return "Loading&#8230;";
        }
    }

    public void setMessage(String message) {
        this.message = message;
        if (getDialog() != null) {
            getDialog().setMessage(message);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_MESSAGE, message);
    }

}

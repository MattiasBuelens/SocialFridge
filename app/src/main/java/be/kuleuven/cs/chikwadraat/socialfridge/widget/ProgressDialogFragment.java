package be.kuleuven.cs.chikwadraat.socialfridge.widget;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import be.kuleuven.cs.chikwadraat.socialfridge.R;

/**
 * Created by Mattias on 1/04/2014.
 */
public class ProgressDialogFragment extends DialogFragment {

    private static final String ARGS_MESSAGE = "message";

    private String message;

    public static ProgressDialogFragment newInstance() {
        return newInstance(null);
    }

    public static ProgressDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(ARGS_MESSAGE, message);
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            message = savedInstanceState.getString(ARGS_MESSAGE);
        } else if (getArguments() != null) {
            message = getArguments().getString(ARGS_MESSAGE);
        }

        if (message == null) {
            message = getActivity().getString(R.string.loading);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getMessage());
        dialog.setIndeterminate(true);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARGS_MESSAGE, message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
        if (getDialog() != null) {
            ((ProgressDialog) getDialog()).setMessage(message);
        }
    }

}

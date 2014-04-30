package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;

/**
 * Dialog fragment for specifying a {@link be.kuleuven.cs.chikwadraat.socialfridge.model.Measure}.
 */
public class MeasureDialogFragment extends DialogFragment implements MeasureDialog.OnMeasureSetListener {

    private static final String MEASURE = "measure";

    private Measure measure;
    private MeasureDialog.OnMeasureSetListener listener;

    public static MeasureDialogFragment newInstance() {
        return newInstance(null);
    }

    public static MeasureDialogFragment newInstance(Measure measure) {
        MeasureDialogFragment fragment = new MeasureDialogFragment();
        fragment.setMeasure(measure);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            measure = savedInstanceState.getParcelable(MEASURE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MeasureDialog(getActivity(), this, measure);
    }

    @Override
    public MeasureDialog getDialog() {
        return (MeasureDialog) super.getDialog();
    }

    public Measure getMeasure() {
        if (getDialog() != null) {
            return getDialog().getMeasure();
        } else {
            return measure;
        }
    }

    public void setMeasure(Measure measure) {
        this.measure = measure;
        if (getDialog() != null) {
            getDialog().setMeasure(measure);
        }
    }

    @Override
    public void onMeasureSet(Measure measure) {
        this.measure = measure;
        if (listener != null) {
            listener.onMeasureSet(measure);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (MeasureDialog.OnMeasureSetListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMeasureSetListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MEASURE, getMeasure());
    }

}

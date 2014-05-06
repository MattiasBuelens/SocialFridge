package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.util.AdapterUtils;

/**
 * Dialog for specifying a {@link be.kuleuven.cs.chikwadraat.socialfridge.model.Measure}.
 */
public class MeasureDialog extends AlertDialog implements DialogInterface.OnClickListener, AdapterView.OnItemSelectedListener, TextView.OnEditorActionListener {

    private static final String MEASURE_VALUE = "measure_value";
    private static final String MEASURE_UNIT = "measure_unit";

    private EditText valueText;
    private Spinner unitSpinner;
    private UnitArrayAdapter unitAdapter;

    private Unit previousMeasureUnit;

    private OnMeasureSetListener callback;

    /**
     * The callback used to indicate the user is done filling in the measure.
     */
    public interface OnMeasureSetListener {

        /**
         * @param measure The measure that was set.
         */
        void onMeasureSet(Measure measure);
    }


    public MeasureDialog(Context context, OnMeasureSetListener callback) {
        this(context, 0, callback);
    }

    public MeasureDialog(Context context, int theme, OnMeasureSetListener callback) {
        super(context, theme);
        this.callback = callback;

        Context themeContext = getContext();
        setButton(BUTTON_POSITIVE, themeContext.getText(R.string.measure_picker_done), this);
        setIcon(0);

        View view = View.inflate(themeContext, R.layout.measure_picker, null);
        setView(view);

        valueText = (EditText) view.findViewById(R.id.measure_value);
        unitSpinner = (Spinner) view.findViewById(R.id.measure_unit);

        valueText.setOnEditorActionListener(this);

        unitAdapter = new UnitArrayAdapter(themeContext);
        unitSpinner.setAdapter(unitAdapter);
        unitSpinner.setOnItemSelectedListener(this);

        valueText.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public MeasureDialog(Context context, OnMeasureSetListener callback, Measure measure) {
        this(context, 0, callback, measure);
    }

    public MeasureDialog(Context context, OnMeasureSetListener callback, double value, Unit unit) {
        this(context, 0, callback, value, unit);
    }

    public MeasureDialog(Context context, int theme, OnMeasureSetListener callback, Measure measure) {
        this(context, theme, callback);
        if (measure != null) {
            update(measure.getValue(), measure.getUnit());
        }
    }

    public MeasureDialog(Context context, int theme, OnMeasureSetListener callback, double value, Unit unit) {
        this(context, theme, callback);
        update(value, unit);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void update(double value, Unit unit) {
        previousMeasureUnit = unit;
        AdapterUtils.setAll(unitAdapter, unit.getQuantity().getUnits());

        setMeasureUnit(unit, false);
        setMeasureValue(value);
    }

    private double getMeasureValue() {
        try {
            return Double.parseDouble(valueText.getText().toString());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    private void setMeasureValue(double value) {
        valueText.setText(getMeasureUnit().formatNumber(value));
    }

    private Unit getMeasureUnit() {
        return (Unit) unitSpinner.getSelectedItem();
    }

    private void setMeasureUnit(Unit unit, boolean convert) {
        if (!convert) {
            previousMeasureUnit = unit;
        }

        int unitPosition = unitAdapter.getPosition(unit);
        unitSpinner.setSelection(unitPosition, true);
    }

    public Measure getMeasure() {
        return new Measure(getMeasureValue(), getMeasureUnit());
    }

    public void setMeasure(Measure measure) {
        update(measure.getValue(), measure.getUnit());
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            finish();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Unit newUnit = getMeasureUnit();
        if (previousMeasureUnit != null && previousMeasureUnit != newUnit) {
            // Convert from previous unit
            Measure measure = new Measure(getMeasureValue(), previousMeasureUnit);
            double convertedValue = measure.getValue(newUnit);
            setMeasureValue(convertedValue);
            previousMeasureUnit = newUnit;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        previousMeasureUnit = null;
    }

    protected void finish() {
        tryNotifyMeasureSet();
        dismiss();
    }

    private void tryNotifyMeasureSet() {
        if (callback != null) {
            valueText.clearFocus();
            unitSpinner.clearFocus();

            callback.onMeasureSet(getMeasure());
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putDouble(MEASURE_VALUE, getMeasureValue());
        state.putString(MEASURE_UNIT, getMeasureUnit().name());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        double measureValue = savedInstanceState.getDouble(MEASURE_VALUE);
        Unit measureUnit = Unit.valueOf(savedInstanceState.getString(MEASURE_UNIT));
        update(measureValue, measureUnit);
    }

    private class UnitArrayAdapter extends ArrayAdapter<Unit> {

        public UnitArrayAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(position, convertView, parent, android.R.layout.simple_spinner_item);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return createViewFromResource(position, convertView, parent, R.layout.spinner_dropdown_item);
        }

        protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
            View v = convertView;
            ViewHolder vh;
            if (v == null) {
                v = View.inflate(getContext(), resource, null);
                vh = new ViewHolder(v);
                v.setTag(vh);
            } else {
                vh = (ViewHolder) v.getTag();
            }

            Unit item = getItem(position);

            String labelText = item.getLabel();

            vh.position = position;
            vh.labelView.setText(labelText);

            return v;
        }

        private class ViewHolder {
            TextView labelView;
            int position;

            private ViewHolder(View v) {
                labelView = (TextView) v.findViewById(android.R.id.text1);
            }
        }

    }


}

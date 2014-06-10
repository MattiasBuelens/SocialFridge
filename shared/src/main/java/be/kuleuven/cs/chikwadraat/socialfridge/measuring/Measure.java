package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

import com.google.common.collect.Ordering;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public class Measure implements Comparable<Measure> {

    private final double standardValue;
    private final Unit unit;

    public Measure(double value, Unit unit) {
        this.unit = checkNotNull(unit);
        this.standardValue = unit.toStandard(value);
    }

    public Unit getUnit() {
        return unit;
    }

    public Quantity getQuantity() {
        return getUnit().getQuantity();
    }

    protected double getStandardValue() {
        return standardValue;
    }

    public double getValue() {
        return getValue(getUnit());
    }

    public double getValue(Unit toUnit) {
        if (!toUnit.getQuantity().equals(getQuantity())) {
            throw new IllegalArgumentException("Cannot convert to different quantity: " + toUnit.getQuantity() + " differs from " + getQuantity());
        }
        return toUnit.fromStandard(getStandardValue());
    }

    public Measure plus(Measure measure) {
        double newValue = getValue() + measure.getValue(getUnit());
        return new Measure(newValue, getUnit());
    }

    public Measure negate() {
        return new Measure(-getValue(), getUnit());
    }

    public Measure minus(Measure measure) {
        return plus(measure.negate());
    }

    public Measure times(double factor) {
        double newValue = getValue() * factor;
        return new Measure(newValue, getUnit());
    }

    public Measure times(long factor) {
        double newValue = getValue() * factor;
        return new Measure(newValue, getUnit());
    }

    public Measure convertTo(Unit toUnit) {
        return new Measure(getValue(toUnit), toUnit);
    }

    @Override
    public String toString() {
        return getUnit().format(getValue());
    }

    @Override
    public int compareTo(Measure other) {
        return ordering.compare(this, other);
    }

    public static final Ordering<Measure> ordering = new Ordering<Measure>() {
        @Override
        public int compare(Measure left, Measure right) {
            return Double.compare(left.getValue(left.getUnit()), right.getValue(left.getUnit()));
        }
    };

}

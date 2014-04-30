package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public class Measure {

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

    @Override
    public String toString() {
        return String.format("%f.2 %s", getValue(), getUnit().getLabel());
    }
}

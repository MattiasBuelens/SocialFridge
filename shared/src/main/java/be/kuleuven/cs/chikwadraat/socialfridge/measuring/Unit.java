package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Unit {

    KILOGRAM("kg", 1d, "%.3f") {
        @Override
        public Quantity getQuantity() {
            return Quantity.MASS;
        }
    },

    GRAM("g", 1000d, "%.1f") {
        @Override
        public Quantity getQuantity() {
            return Quantity.MASS;
        }
    },

    LITRE("l", 1d, "%.3f") {
        @Override
        public Quantity getQuantity() {
            return Quantity.VOLUME;
        }
    },

    MILLILITRE("ml", 1000d, "%.0f") {
        @Override
        public Quantity getQuantity() {
            return Quantity.VOLUME;
        }
    },

    PIECES("pcs", 1d, "%.0f") {
        @Override
        public Quantity getQuantity() {
            return Quantity.DIMENSIONLESS;
        }
    };

    private final String label;
    private final Double conversionFactor;
    private final String numberFormat;

    private Unit(String label, double conversionFactor, String numberFormat) {
        this.label = label;
        this.conversionFactor = conversionFactor;
        this.numberFormat = numberFormat;
    }

    public abstract Quantity getQuantity();

    public String getLabel() {
        return label;
    }

    public double fromStandard(double value) {
        return value * conversionFactor;
    }

    public double toStandard(double value) {
        return value / conversionFactor;
    }

    public String getNumberFormat() {
        return numberFormat;
    }

    public String format(double value) {
        String valueString = String.format(getNumberFormat(), value);
        if (getLabel().isEmpty()) {
            return valueString;
        } else {
            return valueString + " " + getLabel();
        }
    }

}

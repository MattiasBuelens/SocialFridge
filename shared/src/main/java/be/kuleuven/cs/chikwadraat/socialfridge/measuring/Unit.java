package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

import java.util.Locale;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Unit {

    KILOGRAM("kg", 1d, 3) {
        @Override
        public Quantity getQuantity() {
            return Quantity.MASS;
        }
    },

    GRAM("g", 1000d, 0) {
        @Override
        public Quantity getQuantity() {
            return Quantity.MASS;
        }
    },

    LITRE("l", 1d, 3) {
        @Override
        public Quantity getQuantity() {
            return Quantity.VOLUME;
        }
    },

    MILLILITRE("ml", 1000d, 0) {
        @Override
        public Quantity getQuantity() {
            return Quantity.VOLUME;
        }
    },

    PIECES("pcs", 1d, 0) {
        @Override
        public Quantity getQuantity() {
            return Quantity.DIMENSIONLESS;
        }
    };

    private final String label;
    private final Double conversionFactor;
    private final int decimals;

    private Unit(String label, double conversionFactor, int decimals) {
        this.label = label;
        this.conversionFactor = conversionFactor;
        this.decimals = decimals;
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

    public int getNbDecimals() {
        return decimals;
    }

    protected String getNumberFormat() {
        return "%." + getNbDecimals() + "f";
    }

    public String formatNumber(double value) {
        return formatNumber(Locale.ROOT, value);
    }

    public String formatNumber(Locale locale, double value) {
        return String.format(locale, getNumberFormat(), value);
    }

    public String format(double value) {
        return format(Locale.ROOT, value);
    }

    public String format(Locale locale, double value) {
        String valueString = formatNumber(value);
        if (getLabel().isEmpty()) {
            return valueString;
        } else {
            return valueString + " " + getLabel();
        }
    }

    /**
     * For JSTL.
     */
    public String getName() {
        return name();
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Unit {

    KILOGRAM("kg", 1d) {
        @Override
        public Quantity getQuantity() {
            return Quantity.MASS;
        }
    },

    GRAM("g", 1000d) {
        @Override
        public Quantity getQuantity() {
            return Quantity.MASS;
        }
    },

    LITRE("l", 1d) {
        @Override
        public Quantity getQuantity() {
            return Quantity.VOLUME;
        }
    },

    MILLILITRE("ml", 1000d) {
        @Override
        public Quantity getQuantity() {
            return Quantity.VOLUME;
        }
    },

    PIECES("pcs", 1d) {
        @Override
        public Quantity getQuantity() {
            return Quantity.DIMENSIONLESS;
        }
    };

    private final String label;
    private final Double conversionFactor;

    private Unit(String label, double conversionFactor) {
        this.label = label;
        this.conversionFactor = conversionFactor;
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
}

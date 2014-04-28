package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Unit {

    KILOGRAM ("kg", 1.00, Quantity.MASS),

    GRAM ("g", 1000.00, Quantity.MASS),

    LITRE ("l", 1.00, Quantity.VOLUME),

    MILLILITRE ("ml", 100.00, Quantity.VOLUME);

    private String label;

    private Double conversionFactor;

    private Quantity quantity;

    private Unit(String label, Double conversionFactor, Quantity quantity) {
        this.label = label;
        this.conversionFactor = conversionFactor;
        this.quantity = quantity;
    }

    public Double fromStandard(Double value) {
        return value * conversionFactor;
    }

    public Double toStandard(Double value) {
        return value / conversionFactor;
    }
}

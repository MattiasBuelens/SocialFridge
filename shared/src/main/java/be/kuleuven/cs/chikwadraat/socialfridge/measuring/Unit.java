package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Unit {

    KILOGRAM,

    GRAM,

    LITRE,

    MILLILITRE;

    private String label;

    private Double standardConversionFactor;

    private Quantity quantity;
}

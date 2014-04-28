package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public final class Measure {

    private Double value;

    private Unit unit;

    public Measure(Double value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public Measure convertTo(Unit unit) {
        return null;
    }
}

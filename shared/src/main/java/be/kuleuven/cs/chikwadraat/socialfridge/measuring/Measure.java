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

    public Measure convertTo(Unit newUnit) {
        // Example: 100 gram to ton
        Double standardValue = unit.toStandard(value); // 100 gram =  0.1 kilogram
        Double newValue = newUnit.fromStandard(standardValue); // 0.1 kilogram = 0.00001 ton
        return new Measure(newValue, newUnit);
    }
}

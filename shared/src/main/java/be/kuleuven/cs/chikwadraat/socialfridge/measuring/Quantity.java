package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Quantity {

    VOLUME (Unit.LITRE, Arrays.asList(Unit.MILLILITRE)),

    MASS (Unit.KILOGRAM, Arrays.asList(Unit.GRAM));

    private Unit standardUnit;

    private List<Unit> derivedUnits;

    private Quantity(Unit standardUnit, List derivedUnits) {
        this.standardUnit = standardUnit;
        this.derivedUnits = derivedUnits;
    }

    public List<Unit> getUnits() {
        List<Unit> units = new ArrayList<Unit>(derivedUnits);
        units.add(standardUnit);
        return units;
    }
}

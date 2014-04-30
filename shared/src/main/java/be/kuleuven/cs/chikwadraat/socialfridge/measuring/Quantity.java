package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Quantity {

    VOLUME(Unit.LITRE, Arrays.asList(Unit.MILLILITRE)),

    MASS(Unit.KILOGRAM, Arrays.asList(Unit.GRAM)),

    DIMENSIONLESS(Unit.PIECES);

    private Unit standardUnit;
    private ImmutableList<Unit> derivedUnits;

    private Quantity(Unit standardUnit) {
        this(standardUnit, Collections.<Unit>emptyList());
    }

    private Quantity(Unit standardUnit, List<Unit> derivedUnits) {
        this.standardUnit = standardUnit;
        this.derivedUnits = ImmutableList.copyOf(derivedUnits);
    }

    public Unit getStandardUnit() {
        return standardUnit;
    }

    public List<Unit> getUnits() {
        return ImmutableList.<Unit>builder()
                .add(getStandardUnit())
                .addAll(derivedUnits)
                .build();
    }
}

package be.kuleuven.cs.chikwadraat.socialfridge.measuring;

import java.util.ArrayList;

/**
 * Created by Milan Samyn on 28/04/2014.
 */
public enum Quantity {

    VOLUME,

    MASS;

    private Unit standardUnit;

    private ArrayList<Unit> derivedUnits;

    public ArrayList<Unit> getUnits() {
        return null;
    }

    public Double fromStandard(Double value) {
        return 0.0;
    }

    public Double toStandard(Double value) {
        return 0.0;
    }


}

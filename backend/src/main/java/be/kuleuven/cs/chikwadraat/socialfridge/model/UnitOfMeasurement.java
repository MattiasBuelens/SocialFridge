package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by Milan Samyn on 23/04/2014.
 */
@Entity(name = UnitOfMeasurement.KIND)
public class UnitOfMeasurement {

    public static final String KIND = "Unit Of Measurement";

    @Id
    private Long id;

    @Index
    private Unit unit;

    public UnitOfMeasurement() {}

    public UnitOfMeasurement(long id) {
        this.id = id;
    }

    public static Key<Ingredient> getKey(long ingredientID) {
        return Key.create(Ingredient.class, ingredientID);
    }

    public static Ref<Ingredient> getRef(long ingredientID) {
        return Ref.create(getKey(ingredientID));
    }

    /**
     * User ID.
     */
    public Long getID() {
        return id;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getUnitAbbr() {
        String unitAbbr = "";
        switch (unit) {
            case QUANTITY: unitAbbr = "pcs";
            case LITRE: unitAbbr = "l";
            // case CENTILITRE: unitAbbr = "cl";
            case KILOGRAM: unitAbbr = "kg";
            case GRAM: unitAbbr = "g";
        }
        return unitAbbr;
    }

    public static enum Unit {

        QUANTITY,

        LITRE,

        // CENTILITRE,

        KILOGRAM,

        GRAM;

    }
}

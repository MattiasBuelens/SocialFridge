package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem FridgeItem} model.
 */
public class FridgeItem implements Parcelable {

    private String ownerID;
    private final Ingredient ingredient;
    private Measure measure;

    public FridgeItem(Ingredient ingredient, Measure measure) {
        this(null, ingredient, measure);
    }

    public FridgeItem(String ownerID, Ingredient ingredient, Measure measure) {
        this.ownerID = ownerID;
        this.ingredient = ingredient;
        setMeasure(measure);
    }

    public FridgeItem(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem model) {
        this.ownerID = model.getOwnerID();
        this.ingredient = new Ingredient(model.getIngredient());
        setMeasure(model.getStandardAmount(), Unit.valueOf(model.getUnit()));
    }

    public FridgeItem(Parcel in) {
        this.ownerID = in.readString();
        this.ingredient = in.readParcelable(null);
        setMeasure((Measure) in.readParcelable(null));
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Measure getMeasure() {
        return measure;
    }

    public void setMeasure(Measure measure) {
        if (!measure.getUnit().getQuantity().equals(getStandardUnit().getQuantity())) {
            throw new IllegalArgumentException("Cannot convert " + measure.getUnit() + " to " + getStandardUnit());
        }
        this.measure = measure;
    }

    private void setMeasure(double standardAmount, Unit unit) {
        setMeasure(new Measure(standardAmount, getStandardUnit()).convertTo(unit));
    }

    private double getStandardAmount() {
        return getMeasure().getValue(getStandardUnit());
    }

    public Unit getUnit() {
        return getMeasure().getUnit();
    }

    public Unit getStandardUnit() {
        return getIngredient().getQuantity().getStandardUnit();
    }

    @Override
    public String toString() {
        // Used for filtering
        return getIngredient().getName();
    }

    public static List<FridgeItem> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem> items) {
        List<FridgeItem> list = new ArrayList<FridgeItem>();
        if (items != null) {
            for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem item : items) {
                list.add(new FridgeItem(item));
            }
        }
        return list;
    }

    public be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem toEndpoint() {
        be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem model = new be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem();
        model.setOwnerID(getOwnerID())
                .setIngredient(getIngredient().toEndpoint())
                .setStandardAmount(getStandardAmount())
                .setUnit(getUnit().name());
        return model;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getOwnerID());
        dest.writeParcelable(getIngredient(), 0);
        dest.writeParcelable(getMeasure(), 0);
    }

    public static final Creator<FridgeItem> CREATOR = new Creator<FridgeItem>() {

        public FridgeItem createFromParcel(Parcel in) {
            return new FridgeItem(in);
        }

        public FridgeItem[] newArray(int size) {
            return new FridgeItem[size];
        }

    };

}

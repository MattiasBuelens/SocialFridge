package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.DishItem DishItem} model.
 */
public class DishItem implements Parcelable {

    private final Ingredient ingredient;
    private double standardAmount;

    public DishItem(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.DishItem model) {
        this.ingredient = new Ingredient(model.getIngredient());
        setStandardAmount(model.getStandardAmount());
    }

    public DishItem(Parcel in) {
        this.ingredient = in.readParcelable(null);
        setStandardAmount(in.readDouble());
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Measure getMeasure() {
        return new Measure(getStandardAmount(), getStandardUnit());
    }

    public void setMeasure(Measure measure) {
        if (!measure.getUnit().getQuantity().equals(getStandardUnit().getQuantity())) {
            throw new IllegalArgumentException("Cannot convert " + measure.getUnit() + " to " + getStandardUnit());
        }
        this.standardAmount = measure.getValue(getStandardUnit());
    }

    private double getStandardAmount() {
        return standardAmount;
    }

    private void setStandardAmount(double standardAmount) {
        this.standardAmount = standardAmount;
    }

    public Unit getStandardUnit() {
        return getIngredient().getQuantity().getStandardUnit();
    }

    @Override
    public String toString() {
        // Used for filtering
        return getIngredient().getName();
    }

    public static List<DishItem> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.DishItem> items) {
        List<DishItem> list = new ArrayList<DishItem>();
        if (items != null) {
            for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.DishItem item : items) {
                list.add(new DishItem(item));
            }
        }
        return list;
    }

    public be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.DishItem toEndpoint() {
        be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.DishItem model = new be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.DishItem();
        model.setIngredient(getIngredient().toEndpoint())
                .setStandardAmount(getStandardAmount());
        return model;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getIngredient(), 0);
        dest.writeDouble(getStandardAmount());
    }

    public static final Creator<DishItem> CREATOR = new Creator<DishItem>() {

        public DishItem createFromParcel(Parcel in) {
            return new DishItem(in);
        }

        public DishItem[] newArray(int size) {
            return new DishItem[size];
        }

    };

}

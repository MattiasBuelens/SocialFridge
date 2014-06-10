package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.ChecklistItem ChecklistItem} model.
 */
public class ChecklistItem implements Parcelable {

    private final Ingredient ingredient;
    private final double requiredAmount;
    private final double bringAmount;
    private final double missingAmount;

    public ChecklistItem(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.ChecklistItem model) {
        this.ingredient = new Ingredient(model.getIngredient());
        this.requiredAmount = model.getRequiredAmount();
        this.bringAmount = model.getBringAmount();
        this.missingAmount = model.getMissingAmount();
    }

    public ChecklistItem(Parcel in) {
        this.ingredient = in.readParcelable(Ingredient.class.getClassLoader());
        this.requiredAmount = in.readDouble();
        this.bringAmount = in.readDouble();
        this.missingAmount = in.readDouble();
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public Measure getRequired() {
        return new Measure(requiredAmount, getStandardUnit());
    }

    public Measure getBring() {
        return new Measure(bringAmount, getStandardUnit());
    }

    public Measure getMissing() {
        return new Measure(missingAmount, getStandardUnit());
    }

    public Unit getStandardUnit() {
        return getIngredient().getQuantity().getStandardUnit();
    }

    public static List<ChecklistItem> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.ChecklistItem> items) {
        List<ChecklistItem> list = new ArrayList<ChecklistItem>();
        if (items != null) {
            for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.ChecklistItem item : items) {
                list.add(new ChecklistItem(item));
            }
        }
        return list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getIngredient(), 0);
        dest.writeDouble(getRequired().getValue(getStandardUnit()));
        dest.writeDouble(getBring().getValue(getStandardUnit()));
        dest.writeDouble(getMissing().getValue(getStandardUnit()));
    }

    public static final Creator<ChecklistItem> CREATOR = new Creator<ChecklistItem>() {

        public ChecklistItem createFromParcel(Parcel in) {
            return new ChecklistItem(in);
        }

        public ChecklistItem[] newArray(int size) {
            return new ChecklistItem[size];
        }

    };

}

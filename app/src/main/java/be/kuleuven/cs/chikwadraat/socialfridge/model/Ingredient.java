package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Ingredient Ingredient} model.
 */
public class Ingredient implements Parcelable {

    private final long id;
    private final String name;
    private final String category;
    private final Measure defaultMeasure;
    private final String pictureURL;
    private final String thumbnailURL;

    public Ingredient(String name, String category, Measure defaultMeasure) {
        this.id = 0;
        this.name = name;
        this.category = category;
        this.defaultMeasure = defaultMeasure;
        this.pictureURL = "";
        this.thumbnailURL = "";
    }

    public Ingredient(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Ingredient model) {
        this.id = model.getId();
        this.name = model.getName();
        this.category = model.getCategory();
        this.defaultMeasure = new Measure(model.getDefaultAmount(), Unit.valueOf(model.getDefaultUnit()));
        this.pictureURL = model.getPictureURL();
        this.thumbnailURL = model.getThumbnailURL();
    }

    public Ingredient(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.category = in.readString();
        this.defaultMeasure = new Measure(in.readDouble(), Unit.valueOf(in.readString()));
        this.pictureURL = in.readString();
        this.thumbnailURL = in.readString();
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Measure getDefaultMeasure() {
        return defaultMeasure;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    @Override
    public String toString() {
        // Used for filtering
        return getName();
    }

    public be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Ingredient toEndpoint() {
        be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Ingredient model = new be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Ingredient();
        model.setId(getID())
                .setName(getName())
                .setCategory(getCategory())
                .setDefaultAmount(getDefaultMeasure().getValue())
                .setDefaultUnit(getDefaultMeasure().getUnit().name())
                .setPictureURL(getPictureURL())
                .setThumbnailURL(getThumbnailURL());
        return model;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getID());
        dest.writeString(getName());
        dest.writeString(getCategory());
        dest.writeDouble(getDefaultMeasure().getValue());
        dest.writeString(getDefaultMeasure().getUnit().name());
        dest.writeString(getPictureURL());
        dest.writeString(getThumbnailURL());
    }

    public static final Creator<Ingredient> CREATOR = new Creator<Ingredient>() {

        public Ingredient createFromParcel(Parcel in) {
            return new Ingredient(in);
        }

        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }

    };

}

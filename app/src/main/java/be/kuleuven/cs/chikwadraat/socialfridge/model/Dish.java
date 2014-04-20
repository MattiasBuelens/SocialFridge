package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Dish Dish} endpoint model.
 */
public class Dish implements Parcelable {

    private final long id;
    private final String name;
    private final String pictureURL;
    private final String thumbnailURL;

    public Dish(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Dish model) {
        this.id = model.getId();
        this.name = model.getName();
        this.pictureURL = model.getPictureURL();
        this.thumbnailURL = model.getThumbnailURL();
    }

    public Dish(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.pictureURL = in.readString();
        this.thumbnailURL = in.readString();
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPictureURL() {
        return pictureURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public static List<Dish> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Dish> dishes) {
        List<Dish> list = new ArrayList<Dish>();
        if (dishes != null) {
            for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Dish dish : dishes) {
                list.add(new Dish(dish));
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
        dest.writeLong(getID());
        dest.writeString(getName());
        dest.writeString(getPictureURL());
        dest.writeString(getThumbnailURL());
    }

    public static final Parcelable.Creator<Dish> CREATOR = new Parcelable.Creator<Dish>() {

        public Dish createFromParcel(Parcel in) {
            return new Dish(in);
        }

        public Dish[] newArray(int size) {
            return new Dish[size];
        }

    };

}

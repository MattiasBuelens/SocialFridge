package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 */
public class FridgeItem implements Parcelable {

    private final long id;
    private final String name;
    private final int pictureResId;
    private Measure quantity;

    public FridgeItem(String name, int pictureResId, Measure quantity) {
        this.id = 0;
        this.name = name;
        this.pictureResId = pictureResId;
        this.quantity = quantity;
    }

    public FridgeItem(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.pictureResId = in.readInt();
        this.quantity = in.readParcelable(null);
    }

    public long getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPictureResource() {
        return pictureResId;
    }

    public Measure getQuantity() {
        return quantity;
    }

    public void setQuantity(Measure quantity) {
        this.quantity = quantity;
    }

    public Drawable getPicture(Context context) {
        return context.getResources().getDrawable(getPictureResource());
    }

    @Override
    public String toString() {
        // Used for filtering
        return getName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getID());
        dest.writeString(getName());
        dest.writeParcelable(getQuantity(), 0);
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

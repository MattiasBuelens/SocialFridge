package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure Measure} model.
 */
public class Measure extends be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure implements Parcelable {

    public Measure(double value, Unit unit) {
        super(value, unit);
    }

    public Measure(Parcel in) {
        super(in.readDouble(), Unit.valueOf(in.readString()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(getValue());
        dest.writeString(getUnit().name());
    }

    public static final Creator<Measure> CREATOR = new Creator<Measure>() {

        public Measure createFromParcel(Parcel in) {
            return new Measure(in);
        }

        public Measure[] newArray(int size) {
            return new Measure[size];
        }

    };

}

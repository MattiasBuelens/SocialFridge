package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot TimeSlot} endpoint model.
 */
public class TimeSlot implements Parcelable {

    private int beginHour;
    private int endHour;
    private boolean available;

    public TimeSlot(int beginHour, int endHour, boolean isAvailable) {
        this.beginHour = beginHour;
        this.endHour = endHour;
        this.available = isAvailable;
    }

    public TimeSlot(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot model) {
        this.beginHour = model.getBeginHour();
        this.endHour = model.getEndHour();
        this.available = model.getAvailable();
    }

    public TimeSlot(Parcel in) {
        this.beginHour = in.readInt();
        this.endHour = in.readInt();
        this.available = (in.readByte() == 1);
    }

    public int getBeginHour() {
        return beginHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public static List<TimeSlot> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot> slots) {
        List<TimeSlot> list = new ArrayList<TimeSlot>();
        if (slots != null) {
            for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot slot : slots) {
                list.add(new TimeSlot(slot));
            }
        }
        return list;
    }

    public be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot toEndpoint() {
        return new be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot()
                .setBeginHour(getBeginHour()).setEndHour(getEndHour()).setAvailable(isAvailable());
    }

    public static List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot> toEndpoint(List<TimeSlot> slots) {
        List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot> list = new ArrayList<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot>();
        for (TimeSlot slot : slots) {
            list.add(slot.toEndpoint());
        }
        return list;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getBeginHour());
        dest.writeInt(getEndHour());
        dest.writeByte((byte) (isAvailable() ? 1 : 0));
    }

    public static final Creator<TimeSlot> CREATOR = new Creator<TimeSlot>() {

        public TimeSlot createFromParcel(Parcel in) {
            return new TimeSlot(in);
        }

        public TimeSlot[] newArray(int size) {
            return new TimeSlot[size];
        }

    };

}

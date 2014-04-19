package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot TimeSlot} endpoint model.
 */
public class TimeSlot implements Parcelable {

    private Date beginDate;
    private Date endDate;
    private boolean available;

    public TimeSlot(Date beginDate, Date endDate, boolean isAvailable) {
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.available = isAvailable;
    }

    public TimeSlot(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot model) {
        this.beginDate = new Date(model.getBeginDate().getValue());
        this.endDate = new Date(model.getEndDate().getValue());
        this.available = model.getAvailable();
    }

    public TimeSlot(Parcel in) {
        this.beginDate = new Date(in.readLong());
        this.endDate = new Date(in.readLong());
        this.available = (in.readByte() == 1);
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public Date getEndDate() {
        return endDate;
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
        DateTime beginDateTime = new DateTime(getBeginDate(), TimeZone.getDefault());
        DateTime endDateTime = new DateTime(getEndDate(), TimeZone.getDefault());
        return new be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot()
                .setBeginDate(beginDateTime).setEndDate(endDateTime).setAvailable(isAvailable());
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
        dest.writeLong(getBeginDate().getTime());
        dest.writeLong(getEndDate().getTime());
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

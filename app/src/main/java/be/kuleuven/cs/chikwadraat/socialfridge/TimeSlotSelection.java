package be.kuleuven.cs.chikwadraat.socialfridge;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vital.dhaveloose on 31/03/2014.
 */
public class TimeSlotSelection implements Parcelable {

    private int beginHour;
    private int endHour;
    private TimeSlotToggleState state;
    private static final TimeSlotToggleState defaultState = TimeSlotToggleState.UNSPECIFIED;

    public TimeSlotSelection(int beginHour, int endHour, TimeSlotToggleState state) {
        this.beginHour = beginHour;
        this.endHour = endHour;
        this.state = state;
    }

    public TimeSlotSelection(int beginHour, int endHour) {
        this(beginHour, endHour, defaultState);
    }

    protected TimeSlotSelection(Parcel in) {
        this.beginHour = in.readInt();
        this.endHour = in.readInt();
        this.state = TimeSlotToggleState.valueOf(in.readString());
    }

    public int getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(int beginHour) {
        this.beginHour = beginHour;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public TimeSlotToggleState getState() { return state; }

    public void setState(TimeSlotToggleState state) { this.state = state; }

    public boolean isUnspecified() {
        return getState() == TimeSlotToggleState.UNSPECIFIED;
    }

    public boolean isIncluded() {
        return getState() == TimeSlotToggleState.INCLUDED;
    }

    public boolean isExcluded() {
        return getState() == TimeSlotToggleState.EXCLUDED;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getBeginHour());
        dest.writeInt(getEndHour());
        dest.writeString(getState().name());
    }

    public static final Parcelable.Creator<TimeSlotSelection> CREATOR
            = new Parcelable.Creator<TimeSlotSelection>() {
        public TimeSlotSelection createFromParcel(Parcel in) {
            return new TimeSlotSelection(in);
        }

        public TimeSlotSelection[] newArray(int size) {
            return new TimeSlotSelection[size];
        }
    };

}

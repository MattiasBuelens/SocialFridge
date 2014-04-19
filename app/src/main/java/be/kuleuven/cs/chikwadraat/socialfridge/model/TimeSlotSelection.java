package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;


/**
 * Created by vital.dhaveloose on 31/03/2014.
 */
public class TimeSlotSelection implements Parcelable {

    private Date beginDate;
    private Date endDate;
    private State state;
    private static final State defaultState = State.UNSPECIFIED;

    public TimeSlotSelection(Date beginDate, Date endDate, State state) {
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.state = state;
    }

    public TimeSlotSelection(Date beginDate, Date endDate) {
        this(beginDate, endDate, defaultState);
    }

    protected TimeSlotSelection(Parcel in) {
        this.beginDate = new Date(in.readLong());
        this.endDate = new Date(in.readLong());
        this.state = State.valueOf(in.readString());
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isUnspecified() {
        return getState() == State.UNSPECIFIED;
    }

    public boolean isIncluded() {
        return getState() == State.INCLUDED;
    }

    public boolean isExcluded() {
        return getState() == State.EXCLUDED;
    }

    public boolean isDisabled() {
        return getState() == State.DISABLED;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getBeginDate().getTime());
        dest.writeLong(getEndDate().getTime());
        dest.writeString(getState().name());
    }

    public TimeSlot toTimeSlot() {
        return new TimeSlot(getBeginDate(), getEndDate(), isIncluded());
    }

    public static final Creator<TimeSlotSelection> CREATOR = new Creator<TimeSlotSelection>() {

        public TimeSlotSelection createFromParcel(Parcel in) {
            return new TimeSlotSelection(in);
        }

        public TimeSlotSelection[] newArray(int size) {
            return new TimeSlotSelection[size];
        }

    };

    public static enum State {
        UNSPECIFIED, INCLUDED, EXCLUDED, DISABLED
    }

}

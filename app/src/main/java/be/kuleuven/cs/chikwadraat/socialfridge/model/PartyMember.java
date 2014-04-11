package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember PartyMember} endpoint model.
 */
public class PartyMember implements Parcelable {

    private final long partyID;
    private final String userID;
    private final String userName;
    private final boolean isHost;
    private final boolean isInParty;
    private final boolean isInvited;
    private final List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();

    public PartyMember(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember model) {
        this.partyID = model.getPartyID();
        this.userID = model.getUserID();
        this.userName = model.getUserName();
        this.isHost = model.getHost();
        this.isInParty = model.getInParty();
        this.isInvited = model.getInvited();
        this.timeSlots.addAll(TimeSlot.fromEndpoint(model.getTimeSlots()));
    }

    public PartyMember(Parcel in) {
        this.partyID = in.readLong();
        this.userID = in.readString();
        this.userName = in.readString();
        this.isHost = (in.readByte() == 1);
        this.isInParty = (in.readByte() == 1);
        this.isInvited = (in.readByte() == 1);
        in.readTypedList(this.timeSlots, TimeSlot.CREATOR);
    }

    public long getPartyID() {
        return partyID;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public boolean isHost() {
        return isHost;
    }

    public boolean isInParty() {
        return isInParty;
    }

    public boolean isInvited() {
        return isInvited;
    }

    public static List<PartyMember> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember> members) {
        List<PartyMember> list = new ArrayList<PartyMember>();
        if (members != null) {
            for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember member : members) {
                list.add(new PartyMember(member));
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
        dest.writeLong(getPartyID());
        dest.writeString(getUserID());
        dest.writeString(getUserName());
        dest.writeByte((byte) (isHost() ? 1 : 0));
        dest.writeByte((byte) (isInParty() ? 1 : 0));
        dest.writeByte((byte) (isInvited() ? 1 : 0));
        dest.writeTypedList(getTimeSlots());
    }

    public static final Creator<PartyMember> CREATOR = new Creator<PartyMember>() {

        public PartyMember createFromParcel(Parcel in) {
            return new PartyMember(in);
        }

        public PartyMember[] newArray(int size) {
            return new PartyMember[size];
        }

    };

}

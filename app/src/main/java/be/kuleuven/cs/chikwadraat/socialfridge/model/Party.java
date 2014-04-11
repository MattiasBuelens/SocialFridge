package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party Party} endpoint model.
 */
public class Party implements Parcelable {

    private final long id;
    private final String hostID;
    private final List<PartyMember> partners = new ArrayList<PartyMember>();
    private final Status status;
    private final List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();
    private final Date date;
    private final Date dateCreated;

    public Party(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party model) {
        this.id = model.getId();
        this.hostID = model.getHostID();
        this.partners.addAll(PartyMember.fromEndpoint(model.getPartners()));
        this.status = Status.valueOf(model.getStatus());
        this.timeSlots.addAll(TimeSlot.fromEndpoint(model.getTimeSlots()));
        this.date = new Date(model.getDate().getValue());
        this.dateCreated = new Date(model.getDateCreated().getValue());
    }

    public Party(Parcel in) {
        this.id = in.readLong();
        this.hostID = in.readString();
        in.readTypedList(this.partners, PartyMember.CREATOR);
        this.status = Status.valueOf(in.readString());
        in.readTypedList(this.timeSlots, TimeSlot.CREATOR);
        this.date = new Date(in.readLong());
        this.dateCreated = new Date(in.readLong());
    }

    public long getID() {
        return id;
    }

    public String getHostID() {
        return hostID;
    }

    public boolean isHost(User user) {
        return isHost(user.getId());
    }

    public boolean isHost(String userID) {
        return getHostID().equals(userID);
    }

    public PartyMember getHost() {
        for (PartyMember partner : getPartners()) {
            if (isHost(partner.getUserID())) {
                return partner;
            }
        }
        return null;
    }

    public List<PartyMember> getPartners() {
        return partners;
    }

    public boolean isInParty(User user) {
        return isInParty(user.getId());
    }

    public boolean isInParty(String userID) {
        for (PartyMember partner : getPartners()) {
            if (partner.getUserID().equals(userID)) {
                return true;
            }
        }
        return false;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isInviting() {
        return getStatus() == Status.INVITING;
    }

    public boolean isPlanning() {
        return getStatus() == Status.PLANNING;
    }

    public boolean isPlanned() {
        return getStatus() == Status.PLANNED;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public Date getDate() {
        return date;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public static List<Party> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> parties) {
        List<Party> list = new ArrayList<Party>();
        for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party party : parties) {
            list.add(new Party(party));
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
        dest.writeString(getHostID());
        dest.writeTypedList(getPartners());
        dest.writeString(getStatus().name());
        dest.writeTypedList(getTimeSlots());
        dest.writeLong(getDate().getTime());
        dest.writeLong(getDateCreated().getTime());
    }

    public static final Parcelable.Creator<Party> CREATOR = new Parcelable.Creator<Party>() {

        public Party createFromParcel(Parcel in) {
            return new Party(in);
        }

        public Party[] newArray(int size) {
            return new Party[size];
        }

    };

    public enum Status {
        INVITING, PLANNING, PLANNED
    }

}

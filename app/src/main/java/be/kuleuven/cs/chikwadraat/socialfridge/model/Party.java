package be.kuleuven.cs.chikwadraat.socialfridge.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party Party} endpoint model.
 */
public class Party implements Parcelable {

    private final long id;
    private final String hostID;
    private final List<PartyMember> partners = new ArrayList<PartyMember>();
    private final List<PartyMember> invitees = new ArrayList<PartyMember>();
    private final Status status;
    private final List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();
    private final Date date;
    private final Date dateCreated;

    public Party(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party model) {
        this.id = model.getId();
        this.hostID = model.getHostID();
        this.partners.addAll(PartyMember.fromEndpoint(model.getPartners()));
        this.invitees.addAll(PartyMember.fromEndpoint(model.getInvitees()));
        this.status = Status.valueOf(model.getStatus());
        this.timeSlots.addAll(TimeSlot.fromEndpoint(model.getTimeSlots()));
        this.date = new Date(model.getDate().getValue());
        this.dateCreated = new Date(model.getDateCreated().getValue());
    }

    public Party(Parcel in) {
        this.id = in.readLong();
        this.hostID = in.readString();
        in.readTypedList(this.partners, PartyMember.CREATOR);
        in.readTypedList(this.invitees, PartyMember.CREATOR);
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
        return getPartner(getHostID());
    }

    public PartyMember getPartner(User user) {
        return getPartner(user.getId());
    }

    public PartyMember getPartner(String userID) {
        for (PartyMember partner : getPartners()) {
            if (partner.getUserID().equals(userID)) {
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
        return getPartner(userID) != null;
    }

    public PartyMember getInvitee(User user) {
        return getInvitee(user.getId());
    }

    public PartyMember getInvitee(String userID) {
        for (PartyMember invitee : getInvitees()) {
            if (invitee.getUserID().equals(userID)) {
                return invitee;
            }
        }
        return null;
    }

    public List<PartyMember> getInvitees() {
        return invitees;
    }

    public boolean isInvited(User user) {
        return isInvited(user.getId());
    }

    public boolean isInvited(String userID) {
        return getInvitee(userID) != null;
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

    public boolean isDisbanded() {
        return getStatus() == Status.DISBANDED;
    }

    public boolean isCompleted() {
        return isPlanned() && new Date().after(getDate());
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public Date getDate() {
        return date;
    }

    public String formatDate(Context context) {
        if (isPlanned()) {
            // Fully planned, date and time available
            return context.getString(R.string.format_date_and_time,
                    DateFormat.getDateFormat(context).format(getDate()),
                    DateFormat.getTimeFormat(context).format(getDate()));
        } else {
            // Not planned, date only
            return context.getString(R.string.format_date_only,
                    DateFormat.getDateFormat(context).format(getDate()));
        }
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public static List<Party> fromEndpoint(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> parties) {
        List<Party> list = new ArrayList<Party>();
        if (parties != null) {
            for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party party : parties) {
                list.add(new Party(party));
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

        INVITING(R.string.party_status_inviting, R.color.party_status_inviting_background),
        PLANNING(R.string.party_status_planning, R.color.party_status_planning_background),
        PLANNED(R.string.party_status_planned, R.color.party_status_planned_background),
        DISBANDED(R.string.party_status_disbanded, R.color.party_status_disbanded_background);

        private final int stringResID;
        private final int colorResID;

        Status(int stringResID, int colorResID) {
            this.stringResID = stringResID;
            this.colorResID = colorResID;
        }

        public int getStringResource() {
            return stringResID;
        }

        public int getColorResource() {
            return colorResID;
        }

    }

}

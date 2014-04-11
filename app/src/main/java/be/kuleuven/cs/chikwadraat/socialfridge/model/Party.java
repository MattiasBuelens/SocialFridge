package be.kuleuven.cs.chikwadraat.socialfridge.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;

/**
 * Adapter for {@link be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party Party} endpoint model.
 */
public class Party {

    private final long id;
    private final String hostID;
    private final List<PartyMember> partners;
    private final Status status;
    private final List<TimeSlot> timeSlots;
    private final Date date;
    private final Date dateCreated;

    public Party(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party model) {
        this.id = model.getId();
        this.hostID = model.getHostID();
        this.partners = model.getPartners();
        this.status = Status.valueOf(model.getStatus());
        this.timeSlots = model.getTimeSlots();
        this.date = new Date(model.getDate().getValue());
        this.dateCreated = new Date(model.getDateCreated().getValue());
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
        return model.getTimeSlots();
    }

    public Date getDate() {
        return date;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public static List<Party> fromList(List<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> parties) {
        List<Party> list = new ArrayList<Party>();
        for (be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party party : parties) {
            list.add(new Party(party));
        }
        return list;
    }

    public enum Status {
        INVITING, PLANNING, PLANNED
    }

}

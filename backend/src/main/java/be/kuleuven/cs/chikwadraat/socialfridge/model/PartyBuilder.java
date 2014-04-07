package be.kuleuven.cs.chikwadraat.socialfridge.model;

import java.util.Date;
import java.util.List;

/**
 * Builder for creating new parties.
 */
public class PartyBuilder {

    private String hostID;
    private Date date;
    private List<TimeSlot> hostTimeSlots;

    public PartyBuilder() {
    }

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String host) {
        this.hostID = host;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<TimeSlot> getHostTimeSlots() {
        return hostTimeSlots;
    }

    public void setHostTimeSlots(List<TimeSlot> hostTimeSlots) {
        this.hostTimeSlots = hostTimeSlots;
    }

}

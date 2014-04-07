package be.kuleuven.cs.chikwadraat.socialfridge.model;

import java.util.List;

/**
 * Builder for creating new parties.
 */
public class PartyBuilder {

    private String hostID;
    private List<TimeSlot> hostTimeSlots;

    public PartyBuilder() {
    }

    public PartyBuilder(String hostID, List<TimeSlot> hostTimeSlots) {
        this.hostID = hostID;
        this.hostTimeSlots = hostTimeSlots;
    }

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String host) {
        this.hostID = host;
    }

    public List<TimeSlot> getHostTimeSlots() {
        return hostTimeSlots;
    }

    public void setHostTimeSlots(List<TimeSlot> hostTimeSlots) {
        this.hostTimeSlots = hostTimeSlots;
    }

}

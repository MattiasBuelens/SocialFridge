package be.kuleuven.cs.chikwadraat.socialfridge.model;

import java.util.List;

/**
 * Created by Mattias on 7/04/2014.
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

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public List<TimeSlot> getHostTimeSlots() {
        return hostTimeSlots;
    }

    public void setHostTimeSlots(List<TimeSlot> hostTimeSlots) {
        this.hostTimeSlots = hostTimeSlots;
    }

}

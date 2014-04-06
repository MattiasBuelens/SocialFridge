package be.kuleuven.cs.chikwadraat.socialfridge.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Time slot collection. Used as endpoint entity.
 */
public class TimeSlotCollection {

    private List<TimeSlot> list;

    public TimeSlotCollection() {
        this.list = new ArrayList<TimeSlot>();
    }

    public TimeSlotCollection(List<TimeSlot> list) {
        this.list = list;
    }

    public List<TimeSlot> getList() {
        return list;
    }

    public void setList(List<TimeSlot> list) {
        this.list = list;
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import com.googlecode.objectify.annotation.Embed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Time slot selection.
 */
@Embed
public class TimeSlot {

    private int beginHour;
    private int endHour;
    private boolean available;

    public TimeSlot(int beginHour, int endHour, boolean available) {
        this.beginHour = beginHour;
        this.endHour = endHour;
        this.available = available;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    protected TimeSlot copy() {
        return new TimeSlot(getBeginHour(), getEndHour(), isAvailable());
    }

    public static Collection<TimeSlot> merge(Iterable<TimeSlot> slots) {
        Map<Integer, TimeSlot> merged = new HashMap<Integer, TimeSlot>();
        for (TimeSlot slot : slots) {
            TimeSlot mergedSlot = merged.get(slot.getBeginHour());
            if (mergedSlot == null) {
                // New slot
                merged.put(slot.getBeginHour(), slot.copy());
            } else if (slot.isAvailable() && mergedSlot.isAvailable()) {
                // Still available
                mergedSlot.setAvailable(true);
            } else {
                // Unavailable
                mergedSlot.setAvailable(false);
            }
        }
        return merged.values();
    }

    public static Collection<TimeSlot> merge(Iterable<TimeSlot>... slots) {
        return merge(Iterables.concat(slots));
    }

}

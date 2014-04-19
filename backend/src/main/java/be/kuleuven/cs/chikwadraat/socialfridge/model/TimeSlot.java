package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import com.googlecode.objectify.annotation.Embed;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Time slot selection.
 */
@Embed
public class TimeSlot {

    private Date beginDate;
    private Date endDate;
    private boolean available;

    public TimeSlot() {
    }

    public TimeSlot(Date beginDate, Date endDate, boolean available) {
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.available = available;
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

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    protected TimeSlot copy() {
        return new TimeSlot(getBeginDate(), getEndDate(), isAvailable());
    }

    public static Collection<TimeSlot> merge(Iterable<TimeSlot> slots) {
        Map<Date, TimeSlot> merged = new HashMap<Date, TimeSlot>();
        for (TimeSlot slot : slots) {
            TimeSlot mergedSlot = merged.get(slot.getBeginDate());
            if (mergedSlot == null) {
                // New slot
                merged.put(slot.getBeginDate(), slot.copy());
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

    public static final Comparator<TimeSlot> beginDateComparator = new Comparator<TimeSlot>() {

        @Override
        public int compare(TimeSlot left, TimeSlot right) {
            return left.getBeginDate().compareTo(right.getBeginDate());
        }

    };

}

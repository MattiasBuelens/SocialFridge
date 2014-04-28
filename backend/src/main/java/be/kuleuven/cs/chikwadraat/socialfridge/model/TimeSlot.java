package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.IgnoreSave;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

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

        // Cancel upgrade
        this.beginHour = null;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;

        // Cancel upgrade
        this.endHour = null;
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

    @IgnoreSave
    private Integer beginHour;

    @IgnoreSave
    private Integer endHour;

    private Date upgradeDate(Date baseDate, int hour) {
        Calendar calendar = Calendar.getInstance(getUpgradeTimeZone());
        calendar.setTime(baseDate);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return calendar.getTime();
    }

    private boolean needsDateUpgrade() {
        return (beginHour != null) || (endHour != null);
    }

    private void upgradeDates(Date baseDate) {
        if (beginHour != null) {
            setBeginDate(upgradeDate(baseDate, beginHour));
        }
        if (endHour != null) {
            setEndDate(upgradeDate(baseDate, endHour));
        }
    }

    private TimeZone getUpgradeTimeZone() {
        return TimeZone.getTimeZone("Europe/Brussels");
    }

    /**
     * Upgrade the time slots to use full dates instead of just hours.
     *
     * @param slots    The time slots to upgrade.
     * @param partyRef A reference to the owning party.
     *                 The {@link Party#getDate() party date} is used as base date.
     */
    public static void upgradeDates(Collection<TimeSlot> slots, Ref<Party> partyRef) {
        boolean needsUpgrade = false;
        // Check if upgrade needed
        for (TimeSlot slot : slots) {
            if (slot.needsDateUpgrade()) {
                needsUpgrade = true;
                break;
            }
        }
        if (needsUpgrade) {
            // Do upgrade
            Date partyDate = partyRef.get().getDate();
            for (TimeSlot slot : slots) {
                slot.upgradeDates(partyDate);
            }
        }
    }

}

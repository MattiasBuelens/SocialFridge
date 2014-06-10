package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.AlsoLoad;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnLoad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


/**
 * Party.
 */
@Entity(name = Party.KIND)
public class Party {

    public static final String KIND = "Party";

    /*
     * Load groups.
     */

    public static class Everything extends Partial {
    }

    public static class Partial {
    }

    /*
     * Comparators.
     */
    public static final Ordering<Party> dateComparator = new Ordering<Party>() {
        @Override
        public int compare(Party left, Party right) {
            return left.getDate().compareTo(right.getDate());
        }
    };

    /**
     * Party ID.
     */
    @Id
    private Long id;

    /**
     * Host.
     */
    @Load
    private Ref<User> host;

    /**
     * Party status.
     */
    private Status status = Status.INVITING;

    /**
     * Members.
     */
    @Load
    private Set<Ref<PartyMember>> members = new HashSet<Ref<PartyMember>>();

    /**
     * Update recipients.
     */
    @Load(Everything.class)
    @Index
    private Set<Ref<User>> updateRecipients = new HashSet<Ref<User>>();

    /**
     * When partners field is still present,
     * fill in the update recipient fields.
     */
    private void upgradeRecipients(@AlsoLoad("partners") Set<Ref<PartyMember>> partners) {
        for (PartyMember member : getMembers()) {
            if (member.isInParty() || member.isInvited()) {
                User user = member.getUserRef().get();
                addRecipient(user);
            }
        }
    }

    /**
     * Merged time slots from partners.
     */
    private List<TimeSlot> timeSlots = new ArrayList<TimeSlot>();

    /**
     * Upgrade the time slots to use full dates instead of just hours.
     */
    @OnLoad
    private void upgradeTimeSlots() {
        TimeSlot.upgradeDates(getTimeSlots(), getDate());
        for (PartyMember member : getMembers()) {
            TimeSlot.upgradeDates(member.getTimeSlots(), getDate());
        }
    }

    /**
     * Party date.
     */
    @Index
    private Date date;

    /**
     * Date created.
     */
    private Date dateCreated;

    /**
     * Dish.
     */
    @Load
    private Ref<Dish> dish;

    @OnLoad
    private void upgradeDish() {
        if (dish == null) {
            setDishRef(Ref.create(ofy().load().type(Dish.class).keys().first().now()));
        }
    }

    public Party() {
    }

    public Party(Long id) {
        this.id = id;
    }

    /**
     * Party ID.
     */
    public Long getID() {
        return id;
    }

    public static Key<Party> getKey(long partyID) {
        return Key.create(Party.class, partyID);
    }

    public static Ref<Party> getRef(long partyID) {
        return Ref.create(getKey(partyID));
    }

    /**
     * Host.
     */
    public String getHostID() {
        return getHostRef().getKey().getName();
    }

    protected Ref<User> getHostRef() {
        return host;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public User getHost() {
        return getHostRef().get();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public void setHost(User host, List<TimeSlot> timeSlots) {
        // Set as host
        this.host = Ref.create(host);
        PartyMember member = new PartyMember(this, host, PartyMember.Status.HOST);
        member.setTimeSlots(timeSlots);
        // Update
        updateMember(member);
        updatePartner(member);
        addRecipient(host);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<PartyMember> getHostMember() {
        return getMember(getHostID());
    }

    public boolean isHost(String userID) {
        return getHostID().equals(userID);
    }

    public boolean isHost(User user) {
        return isHost(user.getID());
    }

    /**
     * Party status.
     */
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isInviting() {
        return getStatus() == Status.INVITING;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isPlanning() {
        return getStatus() == Status.PLANNING;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isPlanned() {
        return getStatus() == Status.PLANNED;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isDisbanded() {
        return getStatus() == Status.DISBANDED;
    }

    /**
     * Party date.
     * When {@link #isPlanned() not planned yet}, the time part is not yet configured and should be ignored.
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public boolean isCompleted() {
        return isPlanned() && new Date().after(getDate());
    }

    /**
     * Date created.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Members.
     */
    protected Set<Ref<PartyMember>> getMemberKeys() {
        return members;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Map<String, PartyMember> getMembersMap() {
        Map<String, PartyMember> map = new HashMap<String, PartyMember>();
        for (PartyMember member : getMembers()) {
            map.put(member.getUserID(), member);
        }
        return map;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<PartyMember> getMembers() {
        return ofy().load().refs(getMemberKeys()).values();
    }

    public Ref<PartyMember> getMember(User user) {
        return getMember(Ref.create(user));
    }

    public Ref<PartyMember> getMember(Ref<User> userRef) {
        return getMember(userRef.getKey().getName());
    }

    public Ref<PartyMember> getMember(String userID) {
        for (Ref<PartyMember> ref : getMemberKeys()) {
            if (userID.equals(ref.getKey().getName())) {
                return ref;
            }
        }
        return null;
    }

    protected PartyMember updateMember(PartyMember member) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(member.getUserID());
        if (ref != null) {
            // Copy to existing member
            PartyMember existingMember = ref.get();
            existingMember.setUserName(member.getUserName());
            existingMember.setStatus(member.getStatus());
            existingMember.setTimeSlots(member.getTimeSlots());
            member = existingMember;
        } else {
            // Add member
            members.add(Ref.create(member));
        }
        // Save member
        ofy().save().entity(member).now();
        return member;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<String> getMemberIDs() {
        List<String> memberIDs = new ArrayList<String>(getMemberKeys().size());
        for (Ref<PartyMember> ref : getMemberKeys()) {
            memberIDs.add(ref.getKey().getName());
        }
        return memberIDs;
    }

    /**
     * Partners.
     */
    public Collection<PartyMember> getPartners() {
        return Collections2.filter(getMembers(), new Predicate<PartyMember>() {
            @Override
            public boolean apply(@Nullable PartyMember member) {
                return member.isInParty();
            }
        });
    }

    protected void updatePartner(PartyMember member) {
        updateTimeSlots();
    }

    /**
     * Invitees.
     */
    public Collection<PartyMember> getInvitees() {
        return Collections2.filter(getMembers(), new Predicate<PartyMember>() {
            @Override
            public boolean apply(@Nullable PartyMember member) {
                return member.isInvited();
            }
        });
    }

    /**
     * Update recipients.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<Ref<User>> getUpdateRecipientKeys() {
        return updateRecipients;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<User> getUpdateRecipients() {
        return getUpdateRecipientsExcept(new User[0]);
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<User> getUpdateRecipientsExceptHost() {
        return getUpdateRecipientsExcept(getHostRef());
    }

    public Collection<User> getUpdateRecipientsExcept(Ref<User> exclude) {
        Set<Ref<User>> keys = new HashSet<Ref<User>>(getUpdateRecipientKeys());
        keys.remove(exclude);
        return ofy().load().refs(keys).values();
    }

    public Collection<User> getUpdateRecipientsExcept(User exclude) {
        return getUpdateRecipientsExcept(Ref.create(exclude));
    }

    public Collection<User> getUpdateRecipientsExcept(Ref<User>... exclude) {
        Set<Ref<User>> keys = new HashSet<Ref<User>>(getUpdateRecipientKeys());
        for (Ref<User> ref : exclude) {
            keys.remove(ref);
        }
        return ofy().load().refs(keys).values();
    }

    public Collection<User> getUpdateRecipientsExcept(User... exclude) {
        Set<Ref<User>> keys = new HashSet<Ref<User>>(getUpdateRecipientKeys());
        for (User user : exclude) {
            keys.remove(Ref.create(user));
        }
        return ofy().load().refs(keys).values();
    }

    public void addRecipient(User user) {
        updateRecipients.add(Ref.create(user));
        user.addParty(this);
    }

    public void removeRecipient(User user) {
        updateRecipients.remove(Ref.create(user));
        user.removeParty(this);
    }

    /**
     * Merged time slots from partners.
     */
    public Collection<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    protected void setTimeSlots(Collection<TimeSlot> timeSlots) {
        this.timeSlots.clear();
        this.timeSlots.addAll(timeSlots);
        Collections.sort(this.timeSlots, TimeSlot.beginDateComparator);
    }

    public TimeSlot getTimeSlot(Date beginDate, Date endDate) {
        for (TimeSlot slot : getTimeSlots()) {
            if (slot.getBeginDate().equals(beginDate) && slot.getEndDate().equals(endDate)) {
                return slot;
            }
        }
        return null;
    }

    public boolean isAvailable(TimeSlot timeSlot) {
        return isAvailable(timeSlot.getBeginDate(), timeSlot.getEndDate());
    }

    public boolean isAvailable(Date beginHour, Date endHour) {
        TimeSlot slot = getTimeSlot(beginHour, endHour);
        return slot != null && slot.isAvailable();
    }

    protected void updateTimeSlots() {
        // Collect time slots from all partners
        List<TimeSlot> allSlots = new ArrayList<TimeSlot>();
        for (PartyMember partner : getPartners()) {
            allSlots.addAll(partner.getTimeSlots());
        }
        // Merge time slots
        Collection<TimeSlot> mergedSlots = TimeSlot.merge(allSlots);
        // Replace time slots
        setTimeSlots(mergedSlots);
    }

    /**
     * Dish.
     */
    public Dish getDish() {
        return dish.get();
    }

    public void setDish(Dish dish) {
        setDishRef(Ref.create(dish));
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<Dish> getDishRef() {
        return dish;
    }

    public void setDishRef(Ref<Dish> dish) {
        this.dish = dish;
    }

    public List<DishItem> getDishItems(Collection<FridgeItem> fridgeItems) {
        List<DishItem> dishItems = new ArrayList<DishItem>();
        Set<Ref<Ingredient>> ingredients = getDish().getIngredientRefs();
        for (FridgeItem fridgeItem : fridgeItems) {
            if (ingredients.contains(fridgeItem.getIngredientRef())) {
                dishItems.add(DishItem.fromFridge(fridgeItem));
            }
        }
        return dishItems;
    }

    /**
     * Get the current dish item checklist.
     */
    public Collection<ChecklistItem> getChecklist() {
        return getChecklistMap().values();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Map<Ref<Ingredient>, ChecklistItem> getChecklistMap() {
        int nbPersons = getPartners().size();
        Map<Ref<Ingredient>, ChecklistItem> items = new HashMap<Ref<Ingredient>, ChecklistItem>();
        // Initialize with required items
        for (DishItem requiredItem : getDish().getItems()) {
            ChecklistItem item = new ChecklistItem(requiredItem.getIngredient());
            item.setRequiredMeasure(requiredItem.getMeasure().times(nbPersons));
            items.put(requiredItem.getIngredientRef(), item);
        }
        // Add bring items
        for (PartyMember partner : getPartners()) {
            for (DishItem bringItem : partner.getBringItems()) {
                ChecklistItem item = items.get(bringItem.getIngredientRef());
                item.setBringMeasure(item.getBringMeasure().plus(bringItem.getMeasure()));
            }
        }
        return items;
    }

    /**
     * Allocates the item needs over the available bringing partners.
     * <p>The total need for each ingredient is calculated and is assigned to the partners
     * who can contribute the most of that ingredient. Multiple partners may be selected
     * if one partner does not suffice.</p>
     */
    protected void allocateItems() {
        Map<Ref<Ingredient>, ChecklistItem> required = getChecklistMap();
        for (Map.Entry<Ref<Ingredient>, ChecklistItem> entry : required.entrySet()) {
            Ref<Ingredient> ingredientRef = entry.getKey();
            ChecklistItem checklistItem = entry.getValue();
            List<PartnerItem> partnerItems = getPartnersBringing(ingredientRef);
            Measure remaining = checklistItem.getRequiredMeasure();
            for (PartnerItem partnerItem : partnerItems) {
                Measure needed = Measure.ordering.min(remaining, partnerItem.item.getMeasure());
                partnerItem.item.setMeasure(needed);
                remaining = remaining.minus(needed);
            }
        }
    }

    /**
     * Get the partners bringing the given ingredient, greatest amount first.
     *
     * @param ingredientRef The ingredient.
     */
    protected List<PartnerItem> getPartnersBringing(Ref<Ingredient> ingredientRef) {
        // Collect partners who can bring the ingredient
        List<PartnerItem> bringing = new ArrayList<PartnerItem>();
        for (PartyMember partner : getPartners()) {
            DishItem item = partner.getBringItem(ingredientRef);
            if (item != null) {
                bringing.add(new PartnerItem(partner, item));
            }
        }
        // Sort by amount (greatest to lowest)
        Collections.sort(bringing, new Ordering<PartnerItem>() {
            @Override
            public int compare(PartnerItem left, PartnerItem right) {
                return DishItem.amountComparator.compare(left.item, right.item);
            }
        }.reverse());
        return bringing;
    }

    protected class PartnerItem {
        protected final PartyMember partner;
        protected final DishItem item;

        public PartnerItem(PartyMember partner, DishItem item) {
            this.partner = partner;
            this.item = item;
        }
    }

    /**
     * Invite a user to this party.
     *
     * @param invitee The user to invite.
     * @throws IllegalArgumentException If the user cannot be invited because he declined an earlier invite.
     * @throws IllegalStateException    If the party is no longer inviting.
     */
    public void invite(User invitee) throws IllegalArgumentException, IllegalStateException {
        if (!isInviting()) {
            throw new IllegalStateException("Cannot invite user, no longer inviting.");
        }
        Ref<PartyMember> ref = getMember(invitee.getID());
        PartyMember member;
        if (ref != null) {
            member = ref.get();
            if (!member.needsInvite()) {
                // Already in party or invited, don't re-invite
                return;
            }
            if (!member.invite()) {
                // Could not invite, previously declined
                throw new IllegalArgumentException("Cannot invite user, declined an earlier invite.");
            }
        } else {
            // Add invitee
            member = new PartyMember(this, invitee, PartyMember.Status.INVITED);
        }
        updateMember(member);
        addRecipient(invitee);
    }

    /**
     * Cancel an invite for a user.
     *
     * @param invitee The user of whom to cancel the invite.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public void cancelInvite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot cancel user's invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.cancelInvite()) {
            throw new IllegalArgumentException("Cannot cancel user's invite, was not invited or is already in the party.");
        }
        updateMember(member);
        removeRecipient(invitee);
    }

    /**
     * Accept a user's invite.
     *
     * @param invitee    The user of whom to accept the invite.
     * @param timeSlots  The time slots chosen by the user.
     * @param bringItems The items brought by the user.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     * @throws IllegalStateException    If the party is no longer inviting.
     */
    public void acceptInvite(User invitee, List<TimeSlot> timeSlots, List<DishItem> bringItems) throws IllegalArgumentException, IllegalStateException {
        if (!isInviting()) {
            throw new IllegalStateException("Cannot accept invite, no longer inviting.");
        }
        Ref<PartyMember> ref = getMember(invitee);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot accept invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.acceptInvite()) {
            throw new IllegalArgumentException("Cannot accept invite, was not invited or is already in the party.");
        }
        // Set time slots
        member.setTimeSlots(timeSlots);
        member.setBringItems(bringItems);
        updateMember(member);
        updatePartner(member);
    }

    /**
     * Decline a user's invite.
     *
     * @param invitee The user of whom to decline the invite.
     * @throws IllegalArgumentException If the user is already in the party or was not invited.
     */
    public void declineInvite(User invitee) throws IllegalArgumentException {
        Ref<PartyMember> ref = getMember(invitee);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot decline invite, was not invited.");
        }
        PartyMember member = ref.get();
        if (!member.declineInvite()) {
            throw new IllegalArgumentException("Cannot decline invite, was not invited or is already in the party.");
        }
        /*
         * Note: Do NOT remove from members!
         * We should never allow a user to be re-invited after he declined.
         */
        updateMember(member);
        removeRecipient(invitee);
    }

    /**
     * Make a user leave the party.
     *
     * @param user The user to leave.
     * @throws IllegalArgumentException If the user is the host or was not in the party.
     * @throws IllegalStateException    If the party is no longer inviting.
     */
    public void leave(User user) throws IllegalArgumentException, IllegalStateException {
        if (!isInviting()) {
            throw new IllegalStateException("Cannot leave, no longer inviting.");
        }
        Ref<PartyMember> ref = getMember(user);
        if (ref == null) {
            throw new IllegalArgumentException("Cannot leave, was not in the party.");
        }
        PartyMember member = ref.get();
        if (!member.leave()) {
            throw new IllegalArgumentException("Cannot leave, is host or was not in the party.");
        }
        updateMember(member);
        updatePartner(member);
        removeRecipient(user);
    }

    /**
     * Close the invites.
     *
     * @throws IllegalStateException If the party is no longer inviting.
     */
    public void closeInvites() throws IllegalStateException {
        // Party must be inviting
        if (!isInviting()) {
            throw new IllegalStateException("Cannot close invites, no longer inviting.");
        }
        setStatus(Status.PLANNING);
        // Allocate items for dish
        allocateItems();
    }

    /**
     * Plan the party.
     *
     * @param timeSlot The chosen time slot.
     * @throws IllegalArgumentException If not all partners are available on the given time slot.
     * @throws IllegalStateException    If the party is no longer planning.
     */
    public void plan(TimeSlot timeSlot) throws IllegalArgumentException, IllegalStateException {
        // Party must be planning
        if (!isPlanning()) {
            throw new IllegalStateException("Cannot plan, no longer planning.");
        }
        // Time slot must be available
        if (!isAvailable(timeSlot.getBeginDate(), timeSlot.getEndDate())) {
            throw new IllegalArgumentException("Not all partners are available on the given time slot.");
        }
        setStatus(Status.PLANNED);
        setDate(timeSlot.getBeginDate());
    }

    /**
     * Disband the party.
     *
     * @throws IllegalStateException If the party is already completed.
     */
    public void disband() throws IllegalStateException {
        // Party must not be completed
        if (isCompleted()) {
            throw new IllegalStateException("Cannot disband, already completed.");
        }
        setStatus(Status.DISBANDED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Party that = (Party) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static enum Status {

        /**
         * Inviting partners to party.
         */
        INVITING,

        /**
         * Planning a time for the party.
         */
        PLANNING,

        /**
         * Party planned.
         */
        PLANNED,

        /**
         * Party disbanded.
         */
        DISBANDED

    }

}

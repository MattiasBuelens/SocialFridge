package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * FridgeItem.
 */
@Entity(name = FridgeItem.KIND)
public class FridgeItem {

    public static final String KIND = "FridgeItem";

    /**
     * Owner
     */
    @Parent
    private Ref<User> owner;

    /**
     * Ingredient
     */
    @Id
    private Long ingredientID;

    /**
     * Amount in standard unit of ingredient.
     */
    private double standardAmount;

    /**
     * Unit chosen by owner.
     */
    private Unit unit;

    public FridgeItem() {
    }

    public FridgeItem(Ref<User> owner, long ingredientID) {
        this.owner = owner;
        this.ingredientID = ingredientID;
    }

    public FridgeItem(User owner, Ingredient ingredient) {
        this.owner = Ref.create(owner);
        this.ingredientID = ingredient.getID();
    }

    public static Key<FridgeItem> getKey(String userId, Long ingredientId) {
        return Key.create(User.getKey(userId), FridgeItem.class, ingredientId);
    }

    public static Ref<FridgeItem> getRef(String userId, Long ingredientId) {
        return Ref.create(getKey(userId, ingredientId));
    }

    /**
     * Owner.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<User> getOwnerRef() {
        return owner;
    }

    public void setOwner(Ref<User> owner) {
        this.owner = owner;
    }

    public String getOwnerID() {
        return getOwnerRef().getKey().getName();
    }

    public void setOwnerID(String ownerID) {
        setOwner(User.getRef(ownerID));
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public User getOwner() {
        return getOwnerRef().get();
    }

    /**
     * Ingredient.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Long getIngredientID() {
        return ingredientID;
    }

    public void setIngredientID(Long ingredientID) {
        this.ingredientID = ingredientID;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<Ingredient> getIngredientRef() {
        return Ingredient.getRef(getIngredientID());
    }

    public Ingredient getIngredient() {
        return getIngredientRef().get();
    }

    public void setIngredient(Ingredient ingredient) {
        setIngredientID(ingredient.getID());
    }

    /**
     * Amount in standard unit of ingredient.
     */
    public double getStandardAmount() {
        return standardAmount;
    }

    public void setStandardAmount(double standardAmount) {
        this.standardAmount = standardAmount;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Standard unit of ingredient.
     */
    public Unit getStandardUnit() {
        return getIngredient().getQuantity().getStandardUnit();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Measure getMeasure() {
        return new Measure(getStandardAmount(), getUnit());
    }

    public void setMeasure(Measure measure) {
        setStandardAmount(measure.getValue(getStandardUnit()));
        setUnit(measure.getUnit());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FridgeItem item = (FridgeItem) o;
        if (!ingredientID.equals(item.ingredientID)) return false;
        if (!owner.equals(item.owner)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + ingredientID.hashCode();
        return result;
    }

}

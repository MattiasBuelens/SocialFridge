package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

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
    private Long ingredientId;

    /**
     * Value in standard unit of ingredient.
     */
    private double standardValue;

    /**
     * Unit chosen by owner.
     */
    private Unit unit;

    public FridgeItem() {
    }

    public FridgeItem(Ref<User> owner, Long ingredientId) {
        this.owner = owner;
        this.ingredientId = ingredientId;
    }

    public Long getIngredientId() {
        return ingredientId;
    }
}

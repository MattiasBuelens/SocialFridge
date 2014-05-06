package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

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

    public static Key<FridgeItem> getKey(String userId, Long ingredientId) {
        return Key.create(User.getKey(userId), FridgeItem.class, ingredientId);
    }

    public static Ref<FridgeItem> getRef(String userId, Long ingredientId) {
        return Ref.create(getKey(userId, ingredientId));
    }

    public String getOwnerId() {
        return owner.get().getID();
    }

    public Long getIngredientId() {
        return ingredientId;
    }

    /**
     * Ingredient
     */
    public Ingredient getIngredient() {
        Ref<Ingredient> ref = Ingredient.getRef(ingredientId);
        try {
            return (ofy().load().ref(ref)).safe();
        } catch (NotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }
}

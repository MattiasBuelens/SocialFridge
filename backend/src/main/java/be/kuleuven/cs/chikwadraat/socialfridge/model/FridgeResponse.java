package be.kuleuven.cs.chikwadraat.socialfridge.model;

import java.util.Collection;

/**
 * Created by Mattias on 7/05/2014.
 */
public class FridgeResponse {

    private final Collection<FridgeItem> fridge;
    private final Collection<Ingredient> ingredients;

    public FridgeResponse(Collection<FridgeItem> fridge, Collection<Ingredient> ingredients) {
        this.fridge = fridge;
        this.ingredients = ingredients;
    }

    public Collection<FridgeItem> getFridge() {
        return fridge;
    }

    public Collection<Ingredient> getIngredients() {
        return ingredients;
    }

}

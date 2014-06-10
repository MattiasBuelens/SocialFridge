package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


public class IngredientDAO {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Retrieves an ingredient by ingredient ID.
     *
     * @param id The ingredient ID.
     * @return The retrieved ingredient.
     */
    public Ingredient getIngredient(long id) {
        return Ingredient.getRef(id).get();
    }

    /**
     * Retrieves all ingredients.
     *
     * @return The retrieved ingredients.
     */
    public List<Ingredient> getIngredients() {
        return ofy().load().type(Ingredient.class).order("name").list();
    }

    /**
     * Inserts or updates an ingredient.
     *
     * @param ingredient The ingredient to be updated.
     * @return The updated ingredient.
     */
    public Ingredient updateIngredient(final Ingredient ingredient) {
        return ofy().transact(new Work<Ingredient>() {
            @Override
            public Ingredient run() {
                Ingredient storedIngredient = null;
                if (ingredient.getID() != null) {
                    storedIngredient = Ingredient.getRef(ingredient.getID()).get();
                }
                if (storedIngredient != null) {
                    // Copy optional properties from stored ingredient
                    if (ingredient.getPictureKey() == null) {
                        ingredient.setPictureKey(storedIngredient.getPictureKey());
                    } else if (!storedIngredient.getPictureKey().equals(ingredient.getPictureKey())) {
                        // Remove old picture
                        blobstoreService.delete(storedIngredient.getPictureKey());
                    }
                }
                storedIngredient = ingredient;
                ofy().save().entity(storedIngredient).now();
                return storedIngredient;
            }
        });
    }

    /**
     * Removes an ingredient.
     *
     * @param ingredientRef The ingredient to be deleted.
     * @return The deleted ingredient.
     */
    public Ingredient removeIngredient(final Ref<Ingredient> ingredientRef) {
        return ofy().transact(new Work<Ingredient>() {
            @Override
            public Ingredient run() {
                Ingredient ingredient = ingredientRef.get();
                if (ingredient != null) {
                    ofy().delete().entity(ingredient).now();
                }
                return ingredient;
            }
        });
    }

}

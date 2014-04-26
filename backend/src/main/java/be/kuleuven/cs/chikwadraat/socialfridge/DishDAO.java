package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


public class DishDAO {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Retrieves all dishes.
     *
     * @return The retrieved dishes.
     */
    public CollectionResponse<Dish> getDishes() {
        List<Dish> dishes = ofy().load().type(Dish.class).order("name").list();
        return CollectionResponse.<Dish>builder().setItems(dishes).build();
    }

    /**
     * Inserts or updates a dish.
     *
     * @param dish The dish to be updated.
     * @return The updated dish.
     */
    public Dish updateDish(final Dish dish) {
        return ofy().transact(new Work<Dish>() {
            @Override
            public Dish run() {
                Dish storedDish = null;
                if (dish.getID() != null) {
                    storedDish = Dish.getRef(dish.getID()).get();
                }
                if (storedDish != null) {
                    // Copy optional properties from stored dish
                    if (dish.getPictureKey() == null) {
                        dish.setPictureKey(storedDish.getPictureKey());
                    } else if (storedDish.getPictureKey() != null) {
                        // Remove old picture
                        blobstoreService.delete(storedDish.getPictureKey());
                    }
                }
                storedDish = dish;
                ofy().save().entity(storedDish).now();
                return storedDish;
            }
        });
    }

    /**
     * Removes a dish.
     *
     * @param dishRef The dish to be deleted.
     * @return The deleted user.
     */
    public Dish removeDish(final Ref<Dish> dishRef) {
        return ofy().transact(new Work<Dish>() {
            @Override
            public Dish run() {
                Dish dish = dishRef.get();
                if (dish != null) {
                    ofy().delete().entity(dish).now();
                }
                return dish;
            }
        });
    }

}

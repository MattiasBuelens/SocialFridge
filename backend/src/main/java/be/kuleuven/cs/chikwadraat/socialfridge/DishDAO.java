package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;
import static be.kuleuven.cs.chikwadraat.socialfridge.TransactUtils.Work;
import static be.kuleuven.cs.chikwadraat.socialfridge.TransactUtils.transact;


public class DishDAO {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Retrieves a dish by dish ID.
     *
     * @param id The dish ID.
     * @return The retrieved dish.
     */
    public Dish getDish(long id) throws ServiceException {
        Dish dish = getDishUnsafe(id);
        if (dish == null) {
            throw new NotFoundException("Dish not found.");
        }
        return dish;
    }

    /**
     * Retrieves all dishes.
     *
     * @return The retrieved dishes.
     */
    public CollectionResponse<Dish> getDishes() throws ServiceException {
        List<Dish> dishes = ofy().load().type(Dish.class).order("name").list();
        return CollectionResponse.<Dish>builder().setItems(dishes).build();
    }

    /**
     * Inserts or updates a dish.
     *
     * @param dish The dish to be updated.
     * @return The updated dish.
     */
    public Dish updateDish(final Dish dish) throws ServiceException {
        return transact(new Work<Dish, ServiceException>() {
            @Override
            public Dish run() throws ServiceException {
                Dish storedDish = null;
                if (dish.getID() != null) {
                    storedDish = getDishUnsafe(dish.getID());
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
     * @param id The dish ID to be deleted.
     * @return The deleted user.
     */
    public Dish removeDish(final long id) throws ServiceException {
        return transact(new Work<Dish, ServiceException>() {
            @Override
            public Dish run() throws ServiceException {
                Dish dish = getDish(id);
                ofy().delete().entity(dish).now();
                return dish;
            }
        });
    }

    public Dish getDishUnsafe(long id) {
        return ofy().load().type(Dish.class).id(id).now();
    }

}

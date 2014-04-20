package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.util.List;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


@Api(
        name = "endpoint",
        version = "v2",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class DishEndpoint extends BaseEndpoint {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    /**
     * Retrieves a dish by dish ID.
     *
     * @param id The dish ID.
     * @return The retrieved dish.
     */
    @ApiMethod(name = "dishes.getDish", path = "dish/{id}")
    public Dish getDish(@Named("id") long id) throws ServiceException {
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
    @ApiMethod(name = "dishes.getDishes", path = "dish")
    public CollectionResponse<Dish> getDishes() throws ServiceException {
        List<Dish> dishes = ofy().load().type(Dish.class).order("name").list();
        return CollectionResponse.<Dish>builder().setItems(dishes).build();
    }

    /**
     * Inserts or updates a dish.
     * It uses HTTP PUT method.
     *
     * @param dish The dish to be updated.
     * @return The updated dish.
     */
    @ApiMethod(name = "dishes.updateDish", path = "dish")
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
     * It uses HTTP DELETE method.
     *
     * @param id The dish ID to be deleted.
     * @return The deleted user.
     */
    @ApiMethod(name = "dishes.removeDish", path = "dish/{id}")
    public Dish removeDish(final @Named("id") long id) throws ServiceException {
        return transact(new Work<Dish, ServiceException>() {
            @Override
            public Dish run() throws ServiceException {
                Dish dish = getDish(id);
                ofy().delete().entity(dish).now();
                return dish;
            }
        });
    }

    private Dish getDishUnsafe(long id) {
        return ofy().load().type(Dish.class).id(id).now();
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;

import java.util.List;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;


@Api(
        name = "endpoint",
        version = "v3",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class DishEndpoint extends BaseEndpoint {

    private DishDAO dao = new DishDAO();

    /**
     * Retrieves a dish by dish ID.
     *
     * @param id The dish ID.
     * @return The retrieved dish.
     */
    @ApiMethod(name = "dishes.getDish", path = "dish/{id}")
    public Dish getDish(@Named("id") long id) throws ServiceException {
        Dish dish = dao.getDish(id);
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
        List<Dish> dishes = dao.getDishes();
        return CollectionResponse.<Dish>builder().setItems(dishes).build();
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;

import java.util.List;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;


@Api(
        name = "endpoint",
        version = "v2",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class IngredientEndpoint extends BaseEndpoint {

    private IngredientDAO dao = new IngredientDAO();

    /**
     * Retrieves an ingredient by ingredient ID.
     *
     * @param id The ingredient ID.
     * @return The retrieved ingredient.
     */
    @ApiMethod(name = "ingredients.getIngredient", path = "ingredient/{id}")
    public Ingredient getIngredient(@Named("id") long id) throws ServiceException {
        Ingredient ingredient = dao.getIngredient(id);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient not found.");
        }
        return ingredient;
    }

    /**
     * Retrieves all ingredients.
     *
     * @return The retrieved ingredients.
     */
    @ApiMethod(name = "ingredients.getIngredients", path = "ingredient")
    public CollectionResponse<Ingredient> getDishes() throws ServiceException {
        List<Ingredient> ingredients = dao.getIngredients();
        return CollectionResponse.<Ingredient>builder().setItems(ingredients).build();
    }

}

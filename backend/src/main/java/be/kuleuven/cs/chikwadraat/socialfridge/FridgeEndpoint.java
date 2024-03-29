package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeResponse;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

@Api(
        name = "endpoint",
        version = "v3",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class FridgeEndpoint extends BaseEndpoint {

    private FridgeDAO dao = new FridgeDAO();
    private IngredientDAO ingredientDAO = new IngredientDAO();

    /**
     * Retrieves the user's fridge.
     *
     * @param accessToken The access token for authorization.
     * @return The retrieved fridge and ingredients to add.
     */
    @ApiMethod(name = "fridge.getFridge", path = "fridge")
    public FridgeResponse getFridge(@Named("accessToken") String accessToken) throws ServiceException {
        String userID = getUserID(accessToken);
        User user = User.getRef(userID).get();
        if (user == null) {
            throw new NotFoundException("Fridge item not found.");
        }
        // Get user's fridge
        Collection<FridgeItem> fridge = dao.getFridge(user);
        // Get all ingredients
        List<Ingredient> ingredients = ingredientDAO.getIngredients();
        // Remove fridge items from ingredients
        for (FridgeItem item : fridge) {
            ingredients.remove(item.getIngredient());
        }
        return new FridgeResponse(fridge, ingredients);
    }

    /**
     * Inserts or updates a fridge item.
     * It uses HTTP PUT method.
     *
     * @param item        The fridge item to be updated.
     * @param accessToken The access token for authorization.
     * @return The updated fridge item.
     */
    @ApiMethod(name = "fridge.updateItem", path = "fridge/item")
    public FridgeItem updateFridgeItem(final FridgeItem item, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, item.getOwnerID());
        return dao.updateFridgeItem(item);
    }

    /**
     * Removes a fridge item.
     * It uses HTTP DELETE method.
     *
     * @param ingredientId The ID of the ingredient of the fridge item to be deleted.
     * @param accessToken  The access token for authorization.
     * @return The deleted fridge item.
     */
    @ApiMethod(name = "fridge.removeItem", path = "fridge/item/{ingredientId}")
    public FridgeItem removeFridgeItem(final @Named("ingredientId") long ingredientId, @Named("accessToken") String accessToken) throws ServiceException {
        String userID = getUserID(accessToken);
        FridgeItem item = dao.removeFridgeItem(FridgeItem.getRef(userID, ingredientId));
        if (item == null) {
            throw new NotFoundException("Fridge item not found.");
        }
        return item;
    }

}

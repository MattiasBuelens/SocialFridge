package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.NotFoundException;

import java.util.Collection;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

@Api(
        name = "endpoint",
        version = "v2",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class FridgeEndpoint extends BaseEndpoint{

    private FridgeDAO dao = new FridgeDAO();

    /**
     * Retrieves a fridge by user ID.
     *
     * @param id          The user ID.
     * @param accessToken The access token for authorization.
     * @return The retrieved fridge.
     */
    @ApiMethod(name = "users.getUser", path = "user/{id}")
    public CollectionResponse<FridgeItem> getFridge(@Named("id") String id, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, id);
        User user = User.getRef(id).get();
        if (user == null) {
            throw new NotFoundException("Fridge item not found.");
        }
        Collection<FridgeItem> fridge = dao.getFridge(user);
        return CollectionResponse.<FridgeItem>builder().setItems(fridge).build();
    }

    /**
     * Inserts or updates a fridge item.
     * It uses HTTP PUT method.
     *
     * @param item        The fridge item to be updated.
     * @param accessToken The access token for authorization.
     * @return The updated fridge item.
     */
    @ApiMethod(name = "users.updateUser", path = "user")
    public FridgeItem updateFridgeItem(final FridgeItem item, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, item.getOwnerId());
        return dao.updateFridgeItem(item);
    }

    /**
     * Removes a fridge item.
     * It uses HTTP DELETE method.
     *
     * @param userId          The user ID of the owner of the fridge item to be deleted.
     * @param ingredientId    The ID of the ingredient of the fridge item to be deleted.
     * @param accessToken The access token for authorization.
     * @return The deleted fridge item.
     */
    @ApiMethod(name = "users.removeUser", path = "user/{id}")
    public FridgeItem removeFridgeItem(final @Named("userId") String userId, final @Named("ingredientId") Long ingredientId, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, userId);
        FridgeItem item = dao.removeFridgeItem(FridgeItem.getRef(userId,ingredientId));
        if (item == null) {
            throw new NotFoundException("Fridge item not found.");
        }
        return item;
    }
}

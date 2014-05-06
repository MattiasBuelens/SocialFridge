package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.response.CollectionResponse;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;

import java.util.Collection;
import java.util.Set;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

public class FridgeDAO {

    /**
     * Retrieve the fridge items of the given user.
     */
    public CollectionResponse<FridgeItem> getFridge(User user) {
        Collection<FridgeItem> fridge = (ofy().load().refs(user.getFridgeRefs())).values();
        return CollectionResponse.<FridgeItem>builder().setItems(fridge).build();
    }

    /**
     * Inserts or updates a fridge item for a given user.
     */
    public FridgeItem updateFridgeItem(final FridgeItem item, User user) {
        return ofy().transact(new Work<FridgeItem>() {
            @Override
            public FridgeItem run() {
                FridgeItem fridgeItem = null;
                if (item.getOwner() != null && item.getIngredientId() != null) {
                    fridgeItem = FridgeItem.getRef(item.getOwner(), item.getIngredientId()).get();
                }
                fridgeItem = item;
                ofy().save().entity(fridgeItem).now();
                return fridgeItem;
            }
        });
    }

    /**
     * Removes a fridge item.
     */
    public FridgeItem removeFridgeItem(final Ref<FridgeItem> itemRef, User user) {
        return ofy().transact(new Work<FridgeItem>() {
            @Override
            public FridgeItem run() {
                FridgeItem item = itemRef.get();
                if (item != null) {
                    ofy().delete().entity(item).now();
                }
                return item;
            }
        });
    }


}

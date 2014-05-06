package be.kuleuven.cs.chikwadraat.socialfridge;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;

import java.util.Collection;

import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

public class FridgeDAO {

    /**
     * Retrieve the fridge items of the given user.
     */
    public Collection<FridgeItem> getFridge(User user) {
        return ofy().load().refs(user.getFridgeRefs()).values();
    }

    /**
     * Inserts or updates a fridge item for a given user.
     */
    public FridgeItem updateFridgeItem(final FridgeItem item) {
        return ofy().transact(new Work<FridgeItem>() {
            @Override
            public FridgeItem run() {
                FridgeItem fridgeItem = null;
                if (item.getOwnerId() != null && item.getIngredientId() != null) {
                    fridgeItem = FridgeItem.getRef(item.getOwnerId(), item.getIngredientId()).get();
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
    public FridgeItem removeFridgeItem(final Ref<FridgeItem> itemRef) {
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

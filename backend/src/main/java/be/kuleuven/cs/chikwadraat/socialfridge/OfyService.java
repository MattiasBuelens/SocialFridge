package be.kuleuven.cs.chikwadraat.socialfridge;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserMessage;

/**
 * Created by Mattias on 2/04/2014.
 */
public class OfyService {

    /**
     * Register the entity classes.
     */
    static {
        factory().setSaveWithNewEmbedFormat(true);
        ObjectifyService.register(User.class);
        ObjectifyService.register(Party.class);
        ObjectifyService.register(PartyMember.class);
        ObjectifyService.register(UserMessage.class);
        ObjectifyService.register(Dish.class);
        ObjectifyService.register(FridgeItem.class);
        ObjectifyService.register(Ingredient.class);
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

}

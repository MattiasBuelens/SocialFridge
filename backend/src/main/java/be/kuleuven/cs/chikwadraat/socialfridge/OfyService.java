package be.kuleuven.cs.chikwadraat.socialfridge;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

/**
 * Created by Mattias on 2/04/2014.
 */
public class OfyService {

    /**
     * Register the entity classes.
     */
    static {
        ObjectifyService.register(User.class);
        ObjectifyService.register(Party.class);
        ObjectifyService.register(PartyMember.class);
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

}

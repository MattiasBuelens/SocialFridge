package be.kuleuven.cs.chikwadraat.socialfridge;

import com.googlecode.objectify.ObjectifyService;

import be.kuleuven.cs.chikwadraat.socialfridge.auth.FacebookAuthEndpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserDevice;

/**
 * Base class for endpoints.
 */
public abstract class BaseEndpoint extends FacebookAuthEndpoint {

    static {
        ObjectifyService.register(User.class);
        ObjectifyService.register(UserDevice.class);
        ObjectifyService.register(Party.class);
        ObjectifyService.register(PartyMember.class);
    }

}

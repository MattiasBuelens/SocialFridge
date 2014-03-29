package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;

import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import be.kuleuven.cs.chikwadraat.socialfridge.auth.FacebookAuthEndpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

@Api(
        name = "parties",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class PartyEndpoint extends FacebookAuthEndpoint {

    /**
     * Retrieves a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     * @return The retrieved party.
     */
    @ApiMethod(name = "getParty", path = "party/{partyID}")
    public Party getParty(@Named("partyID") long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        Party party = getParty(partyID);
        if (party == null) {
            throw new ConflictException(new EntityNotFoundException("Party not found"));
        }
        checkAccess(accessToken, party.getMemberIDs());
        return party;
    }

    /**
     * Inserts a party.
     * It uses HTTP POST method.
     *
     * @param party       The user to be updated.
     * @param accessToken The access token for authorization.
     * @return The inserted party.
     */
    @ApiMethod(name = "insertParty", path = "party")
    public Party insertParty(Party party, @Named("accessToken") String accessToken) throws ServiceException {
        checkAccess(accessToken, party.getHostID());
        EntityManager mgr = getEntityManager();
        try {
            if (containsParty(mgr, party)) {
                throw new ConflictException(new EntityExistsException("Party already exists"));
            }
            mgr.persist(party);
        } finally {
            mgr.close();
        }
        return party;
    }

    /**
     * Invite a user to a party.
     *
     * @param partyID     The party ID.
     * @param friendID    The user ID of the friend to invite.
     * @param accessToken The access token for authorization.
     * @return The inserted party.
     */
    @ApiMethod(name = "inviteToParty", path = "party/{partyID}/invite")
    public void inviteToParty(@Named("partyID") long partyID, @Named("userID") String friendID, @Named("accessToken") String accessToken) throws ServiceException {
        // Check if friend exists
        User friend = getUser(friendID);
        if (friend == null) {
            throw new ConflictException(new EntityNotFoundException("Friend not found"));
        }
        // Check if user is befriended with friend
        String userID = getUserID(accessToken);
        if (!isBefriendedWith(friendID, accessToken)) {
            throw new ConflictException(new EntityNotFoundException("User must be befriended with friend"));
        }

        EntityManager mgr = getEntityManager();
        try {
            // Check if party exists
            Party party = getParty(mgr, partyID);
            if (party == null) {
                throw new ConflictException(new EntityNotFoundException("Party not found"));
            }
            // User must be host
            if (!userID.equals(party.getHostID())) {
                throw new UnauthorizedException("User must be party host to invite friends");
            }
            // Add to invitees
            party.invite(friend);
            mgr.persist(party);
        } finally {
            mgr.close();
        }
        // TODO Send invite to friend
        // TODO Send update to all party members
    }

    protected Party getParty(long partyID) {
        EntityManager mgr = getEntityManager();
        try {
            return getParty(mgr, partyID);
        } finally {
            mgr.close();
        }
    }

    protected Party getParty(EntityManager mgr, long partyID) {
        return mgr.find(Party.class, Party.getKey(partyID));
    }

    protected boolean containsParty(EntityManager mgr, Party party) {
        Party item = mgr.find(Party.class, party.getKey());
        return item != null;
    }

    protected User getUser(String userID) {
        EntityManager mgr = getEntityManager();
        try {
            return mgr.find(User.class, User.getKey(userID));
        } finally {
            mgr.close();
        }
    }

    protected boolean isBefriendedWith(String friendID, String userAccessToken) {
        return getAPI().isBefriendedWith(friendID, userAccessToken);
    }

    protected static EntityManager getEntityManager() {
        return EMF.get().createEntityManager();
    }

}

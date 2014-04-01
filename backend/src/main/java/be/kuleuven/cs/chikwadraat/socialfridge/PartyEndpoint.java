package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.Work;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Api(
        name = "parties",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class PartyEndpoint extends BaseEndpoint {

    /**
     * Retrieves a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     * @return The retrieved party.
     */
    @ApiMethod(name = "getParty", path = "party/{partyID}")
    public Party getParty(@Named("partyID") long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        Party party = getParty(partyID, true);
        if (party == null) {
            throw new ConflictException(new EntityNotFoundException("Party not found"));
        }
        checkAccess(accessToken, party.getMemberIDs());
        return party;
    }

    /**
     * Insert a party.
     *
     * @param party       The party to be inserted.
     * @param accessToken The access token for authorization.
     * @return The inserted party.
     */
    @ApiMethod(name = "insertParty", path = "party")
    public Party insertParty(final Party party, @Named("accessToken") String accessToken) throws ServiceException {
        String userID = party.getHostID();
        checkAccess(accessToken, userID);
        if (party.getID() != null) {
            throw new EntityExistsException("Party already exists");
        }
        final User user = getUser(userID);
        try {
            return ofy().transact(new Work<Party>() {
                @Override
                public Party run() {
                    // Save party
                    ofy().save().entity(party).now();
                    // Configure the host
                    PartyMember hostMember = party.setHost(user);
                    // Save again
                    ofy().save().entities(party, hostMember).now();
                    return party;
                }
            });
        } catch (EntityExistsException e) {
            throw new ConflictException(e);
        }
    }

    /**
     * Invite a user to a party.
     *
     * @param partyID     The party ID.
     * @param friendID    The user ID of the friend to invite.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "inviteToParty", path = "party/{partyID}/invite")
    public void inviteToParty(@Named("partyID") final long partyID, @Named("userID") final String friendID, @Named("accessToken") String accessToken) throws ServiceException {
        // Check if friend exists
        final User friend = getUser(friendID);
        if (friend == null) {
            throw new ConflictException(new EntityNotFoundException("Friend not found"));
        }
        // Check if user is befriended with friend
        final String userID = getUserID(accessToken);
        if (!isBefriendedWith(friendID, accessToken)) {
            throw new ConflictException(new EntityNotFoundException("User must be befriended with friend"));
        }

        try {
            ofy().transact(new VoidWork() {
                @Override
                public void vrun() {
                    // Check if exists
                    Party party = getParty(partyID, true);
                    if (party == null) {
                        throw new EntityExistsException("Party not found");
                    }
                    // User must be host
                    if (!userID.equals(party.getHostID())) {
                        throw new IllegalStateException("User must be party host to invite friends");
                    }
                    // Add to invitees
                    PartyMember member = party.invite(friend);
                    // Save
                    ofy().save().entities(party, member).now();
                }
            });
        } catch (EntityExistsException e) {
            throw new ConflictException(e);
        } catch (IllegalStateException e) {
            throw new UnauthorizedException(e);
        }

        // TODO Send invite to friend
        // TODO Send update to all party members
    }

    /**
     * Accept an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "acceptInvite", path = "party/{partyID}/acceptInvite")
    public void acceptInvite(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        // TODO Add time slots parameter
        // TODO Implement
    }

    /**
     * Decline an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "declineInvite", path = "party/{partyID}/declineInvite")
    public void declineInvite(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        // TODO Implement
    }

    /**
     * Retrieve candidates for a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     * @return The list of candidates.
     */
    @ApiMethod(name = "getCandidates", path = "party/{partyID}/candidates")
    public List<PartyMember> getCandidates(@Named("partyID") long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        String userID = getUserID(accessToken);
        // Check if party exists
        Party party = getParty(partyID, true);
        if (party == null) {
            throw new ConflictException(new EntityNotFoundException("Party not found"));
        }
        // User must be host
        if (!userID.equals(party.getHostID())) {
            throw new UnauthorizedException("User must be party host to invite friends");
        }
        // Retrieve current members
        Map<String, PartyMember> members = party.getMembersMap();
        // Retrieve friends using our application
        Collection<User> friends = getAppUsers(getAPI().getFriends(accessToken));
        List<PartyMember> candidates = new ArrayList<PartyMember>();
        for (User friend : friends) {
            PartyMember member = members.get(friend.getID());
            if (member == null) {
                // Not yet invited
                candidates.add(new PartyMember(party, friend, PartyMember.Status.CANDIDATE));
            } else if (member.canInvite() || member.isInvited()) {
                // Can invite or already invited
                candidates.add(member);
            }
        }
        return candidates;
    }

    protected Party getParty(long partyID, boolean full) throws PersistenceException {
        Party party = ofy().load()
                .group(full ? Party.Everything.class : Party.Partial.class)
                .type(Party.class)
                .id(partyID)
                .now();
        if (party == null) {
            throw new EntityNotFoundException("Party not found.");
        }
        return party;
    }

    protected User getUser(String userID) {
        return ofy().load().type(User.class).id(userID).now();
    }

    protected boolean isBefriendedWith(String friendID, String userAccessToken) {
        return getAPI().isBefriendedWith(friendID, userAccessToken);
    }

    protected Collection<User> getAppUsers(Iterable<com.restfb.types.User> facebookUsers) {
        List<String> facebookIDs = new ArrayList<String>();
        for (com.restfb.types.User facebookUser : facebookUsers) {
            facebookIDs.add(facebookUser.getId());
        }
        return ofy().load().type(User.class).ids(facebookIDs).values();
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotCollection;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


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
    @ApiMethod(name = "getParty", path = "party/{partyID}", httpMethod = ApiMethod.HttpMethod.GET)
    public Party getParty(@Named("partyID") long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        Party party = getParty(partyID, false);
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
    @ApiMethod(name = "insertParty", path = "party", httpMethod = ApiMethod.HttpMethod.POST)
    public Party insertParty(final Party party, @Named("accessToken") String accessToken) throws ServiceException {
        String userID = party.getHostID();
        checkAccess(accessToken, userID);
        if (party.getID() != null) {
            throw new NotFoundException("Party already exists");
        }
        final User user = getUser(userID);
        return transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                // Save party first (needed to generate a key)
                ofy().save().entity(party).now();
                // Configure the host
                PartyMember member = party.setHost(user);
                // Save again
                ofy().save().entities(party, member, user).now();
                return party;
            }
        });
    }

    /**
     * Invite a user to a party.
     *
     * @param partyID     The party ID.
     * @param friendID    The user ID of the friend to invite.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "invite", path = "party/{partyID}/invite/{friendID}", httpMethod = ApiMethod.HttpMethod.GET)
    public void invite(@Named("partyID") final long partyID, @Named("friendID") final String friendID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        // Check if friend exists
        final User friend = getUser(friendID);
        if (friend == null) {
            throw new NotFoundException("Friend not found");
        }
        // Check if user is befriended with friend
        if (!isBefriendedWith(friendID, accessToken)) {
            throw new UnauthorizedException("User must be befriended with friend");
        }
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                Party party = getParty(partyID, true);
                // User must be host
                if (!userID.equals(party.getHostID())) {
                    throw new UnauthorizedException("User must be party host to invite friends");
                }
                // Add to invitees
                PartyMember member = party.invite(friend);
                // Save
                ofy().save().entities(party, member, friend).now();
            }
        });

        // TODO Send invite to friend
        // TODO Send update to all party members
    }

    /**
     * Cancel a user's invite to a party.
     *
     * @param partyID     The party ID.
     * @param friendID    The user ID of the friend to invite.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "cancelInvite", path = "party/{partyID}/invite/{friendID}", httpMethod = ApiMethod.HttpMethod.DELETE)
    public void cancelInvite(@Named("partyID") final long partyID, @Named("friendID") final String friendID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        // Check if friend exists
        final User friend = getUser(friendID);
        if (friend == null) {
            throw new NotFoundException("Friend not found");
        }
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                Party party = getParty(partyID, true);
                // User must be host
                if (!userID.equals(party.getHostID())) {
                    throw new UnauthorizedException("User must be party host to manage invites");
                }
                // Cancel invite
                PartyMember member = party.cancelInvite(friend);
                // Save
                ofy().save().entities(party, member, friend).now();
            }
        });
    }

    /**
     * Accept an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "acceptInvite", path = "party/{partyID}/acceptInvite", httpMethod = ApiMethod.HttpMethod.POST)
    public void acceptInvite(@Named("partyID") final long partyID, final TimeSlotCollection timeSlots, @Named("accessToken") String accessToken) throws ServiceException {
        // TODO Add time slots parameter
        final String userID = getUserID(accessToken);
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                User user = getUser(userID);
                if (user == null) {
                    throw new NotFoundException("User not found");
                }
                Party party = getParty(partyID, true);
                // Accept invite
                PartyMember member = party.acceptInvite(user, timeSlots.getList());
                // Save
                ofy().save().entities(party, member, user).now();
            }
        });
    }

    /**
     * Decline an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "declineInvite", path = "party/{partyID}/declineInvite", httpMethod = ApiMethod.HttpMethod.GET)
    public void declineInvite(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                User user = getUser(userID);
                if (user == null) {
                    throw new NotFoundException("User not found");
                }
                Party party = getParty(partyID, true);
                // Decline invite
                PartyMember member = party.declineInvite(user);
                // Save
                ofy().save().entities(party, member, user).now();
            }
        });
    }

    /**
     * Leave a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "leaveParty", path = "party/{partyID}/leave", httpMethod = ApiMethod.HttpMethod.GET)
    public void leaveParty(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                User user = getUser(userID);
                if (user == null) {
                    throw new NotFoundException("User not found");
                }
                Party party = getParty(partyID, true);
                // Leave
                PartyMember member = party.leave(user);
                // Save
                ofy().save().entities(party, member, user).now();
            }
        });
    }

    /**
     * Retrieve candidates for a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     * @return The list of candidates.
     */
    @ApiMethod(name = "getCandidates", path = "party/{partyID}/candidates", httpMethod = ApiMethod.HttpMethod.GET)
    public List<PartyMember> getCandidates(@Named("partyID") long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        String userID = getUserID(accessToken);
        Party party = getParty(partyID, true);
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

    protected Party getParty(long partyID, boolean full) throws ServiceException {
        Party party = ofy().load()
                .group(full ? Party.Everything.class : Party.Partial.class)
                .type(Party.class)
                .id(partyID)
                .now();
        if (party == null) {
            throw new NotFoundException("Party not found.");
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

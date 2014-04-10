package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.messaging.PartyUpdateReason;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyBuilder;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyCollection;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotCollection;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.model.UserMessage;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


@Api(
        name = "endpoint",
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
    @ApiMethod(name = "parties.getParty", path = "party/{partyID}", httpMethod = ApiMethod.HttpMethod.GET)
    public Party getParty(@Named("partyID") long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        Party party = getParty(partyID, false);
        checkAccess(accessToken, party.getMemberIDs());
        return party;
    }

    /**
     * Retrieves all parties for a user.
     *
     * @param accessToken The access token for authorization.
     * @return The parties of a user.
     */
    @ApiMethod(name = "parties.getParties", path = "parties", httpMethod = ApiMethod.HttpMethod.GET)
    public PartyCollection getParties(@Named("accessToken") String accessToken) throws ServiceException {
        String userID = getUserID(accessToken);
        User user = getUser(userID);
        List<Party> parties = new ArrayList<Party>(user.getParties());
        return new PartyCollection(parties);
    }

    /**
     * Insert a party.
     *
     * @param builder     A builder describing the party to be inserted.
     * @param accessToken The access token for authorization.
     * @return The inserted party.
     */
    @ApiMethod(name = "parties.insertParty", path = "party", httpMethod = ApiMethod.HttpMethod.POST)
    public Party insertParty(final PartyBuilder builder, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = builder.getHostID();
        checkAccess(accessToken, userID);
        return transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                User user = getUser(userID);
                if (user == null) {
                    throw new NotFoundException("User not found.");
                }
                // Create party
                final Party party = new Party();
                party.setDateCreated(new Date());
                // Party date
                party.setDate(builder.getDate());
                // TODO Dish
                // Save party first (needed to generate a key)
                ofy().save().entity(party).now();
                // Configure the host
                party.setHost(user, builder.getHostTimeSlots());
                // Save again
                ofy().save().entities(party, user).now();
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
    @ApiMethod(name = "parties.invite", path = "party/{partyID}/invite/{friendID}", httpMethod = ApiMethod.HttpMethod.GET)
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
                party.invite(friend);
                // Save
                ofy().save().entities(party, friend).now();
            }
        });

        // Send invite to friend
        User host = getUser(userID);
        List<UserMessage> messages = Messages.partyInvited(partyID)
                .host(host)
                .invitee(friend)
                .recipients(friend)
                .build();
        new UserMessageEndpoint().addMessages(messages);
    }

    /**
     * Cancel a user's invite to a party.
     *
     * @param partyID     The party ID.
     * @param friendID    The user ID of the friend to invite.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.cancelInvite", path = "party/{partyID}/invite/{friendID}", httpMethod = ApiMethod.HttpMethod.DELETE)
    public void cancelInvite(@Named("partyID") final long partyID, @Named("friendID") final String friendID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        // Check if friend exists
        final User friend = getUserUnsafe(friendID);
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
                party.cancelInvite(friend);
                // Save
                ofy().save().entities(party, friend).now();
            }
        });

        // Send cancel invite to friend
        User host = getUser(userID);
        List<UserMessage> messages = Messages.partyInviteCanceled(partyID)
                .host(host)
                .invitee(friend)
                .recipients(friend)
                .build();
        new UserMessageEndpoint().addMessages(messages);
    }

    /**
     * Accept an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.acceptInvite", path = "party/{partyID}/acceptInvite", httpMethod = ApiMethod.HttpMethod.POST)
    public void acceptInvite(@Named("partyID") final long partyID, final TimeSlotCollection timeSlots, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                User user = getUser(userID);
                Party party = getParty(partyID, true);
                // Accept invite
                party.acceptInvite(user, timeSlots.getList());
                // Save
                ofy().save().entities(party, user).now();
                return party;
            }
        });
        // Send update to party members
        User user = getUser(userID);
        List<UserMessage> messages = Messages.partyUpdated(partyID)
                .reason(PartyUpdateReason.JOINED)
                .reasonUser(user)
                .recipients(party.getUpdateUsers())
                .build();
        new UserMessageEndpoint().addMessages(messages);
    }

    /**
     * Decline an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.declineInvite", path = "party/{partyID}/declineInvite", httpMethod = ApiMethod.HttpMethod.GET)
    public void declineInvite(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                User user = getUser(userID);
                Party party = getParty(partyID, true);
                // Decline invite
                party.declineInvite(user);
                // Save
                ofy().save().entities(party, user).now();
                return party;
            }
        });
        // TODO Send update to host?
    }

    /**
     * Leave a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.leaveParty", path = "party/{partyID}/leave", httpMethod = ApiMethod.HttpMethod.GET)
    public void leaveParty(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                User user = getUser(userID);
                Party party = getParty(partyID, true);
                // Leave
                party.leave(user);
                // Save
                ofy().save().entities(party, user).now();
                return party;
            }
        });
        // Send update to party members
        User user = getUser(userID);
        List<UserMessage> messages = Messages.partyUpdated(partyID)
                .reason(PartyUpdateReason.LEFT)
                .reasonUser(user)
                .recipients(party.getUpdateUsers())
                .build();
        new UserMessageEndpoint().addMessages(messages);
    }

    /**
     * Retrieve candidates for a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     * @return The list of candidates.
     */
    @ApiMethod(name = "parties.getCandidates", path = "party/{partyID}/candidates", httpMethod = ApiMethod.HttpMethod.GET)
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

    /**
     * Close invites for a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.closeInvites", path = "party/{partyID}/closeInvites", httpMethod = ApiMethod.HttpMethod.GET)
    public void closeInvites(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        transact(new VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                Party party = getParty(partyID, true);
                // User must be host
                if (!userID.equals(party.getHostID())) {
                    throw new UnauthorizedException("User must be party host to close invites");
                }
                // Party must be inviting
                if (!party.isInviting()) {
                    throw new ConflictException("Party must be still inviting");
                }
                // Set planning
                party.setPlanning();
                // Save
                ofy().save().entities(party).now();
            }
        });
    }


    /**
     * Plan a party.
     *
     * @param partyID     The party ID.
     * @param timeSlot    The chosen time slot.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.plan", path = "party/{partyID}/plan", httpMethod = ApiMethod.HttpMethod.POST)
    public void plan(@Named("partyID") final long partyID, final TimeSlot timeSlot, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                Party party = getParty(partyID, true);
                // User must be host
                if (!userID.equals(party.getHostID())) {
                    throw new UnauthorizedException("User must be party host to plan");
                }
                // Party must be planning
                if (!party.isPlanning()) {
                    throw new ConflictException("Party must be still planning");
                }
                // Time slot must be available
                if (!party.isAvailable(timeSlot.getBeginHour(), timeSlot.getEndHour())) {
                    throw new ConflictException("Not all partners are available on the given time slot");
                }
                // Set planned
                party.setPlanned(timeSlot);
                // Save
                ofy().save().entities(party).now();
                return party;
            }
        });
        // Send update to party members
        List<UserMessage> messages = Messages.partyUpdated(partyID)
                .reason(PartyUpdateReason.DONE)
                .recipients(party.getUpdateUsers())
                .build();
        new UserMessageEndpoint().addMessages(messages);
    }

    protected Party getParty(long partyID, boolean full) throws ServiceException {
        Party party = getPartyUnsafe(partyID, full);
        if (party == null) {
            throw new NotFoundException("Party not found.");
        }
        return party;
    }

    protected Party getPartyUnsafe(long partyID, boolean full) {
        return ofy().load()
                .group(full ? Party.Everything.class : Party.Partial.class)
                .type(Party.class)
                .id(partyID)
                .now();
    }

    protected User getUser(String userID) throws ServiceException {
        User user = getUserUnsafe(userID);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    protected User getUserUnsafe(String userID) {
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

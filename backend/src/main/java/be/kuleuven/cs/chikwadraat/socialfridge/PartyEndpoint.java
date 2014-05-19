package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import be.kuleuven.cs.chikwadraat.socialfridge.messaging.PartyUpdateReason;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyBuilder;
import be.kuleuven.cs.chikwadraat.socialfridge.model.PartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlot;
import be.kuleuven.cs.chikwadraat.socialfridge.model.TimeSlotCollection;
import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;
import static be.kuleuven.cs.chikwadraat.socialfridge.TransactUtils.Work;
import static be.kuleuven.cs.chikwadraat.socialfridge.TransactUtils.transact;


@Api(
        name = "endpoint",
        version = "v3",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class PartyEndpoint extends BaseEndpoint {

    private final UserMessageDAO messageDAO = new UserMessageDAO();

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
                // Create party
                final Party party = new Party();
                party.setDateCreated(new Date());
                // Party date
                party.setDate(builder.getDate());
                // Dish
                party.setDishRef(Dish.getRef(builder.getDishID()));
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
    public Party invite(@Named("partyID") final long partyID, @Named("friendID") final String friendID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        // Check if user is befriended with friend
        if (!isBefriendedWith(friendID, accessToken)) {
            throw new UnauthorizedException("User must be befriended with friend");
        }
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                // Check if friend exists
                final User friend = getUserUnsafe(friendID);
                if (friend == null) {
                    throw new NotFoundException("Friend not found");
                }
                Party party = getParty(partyID, true);
                // User must be host
                if (!party.isHost(userID)) {
                    throw new UnauthorizedException("User must be party host to invite friends");
                }
                // Add to invitees
                try {
                    party.invite(friend);
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Save
                ofy().save().entities(party, friend).now();
                return party;
            }
        });
        // Send invite to friend
        User friend = getUser(friendID);
        messageDAO.addMessages(Messages.partyInvited(party)
                .invitee(friend)
                .recipients(friend)
                .build());
        return party;
    }

    /**
     * Cancel a user's invite to a party.
     *
     * @param partyID     The party ID.
     * @param friendID    The user ID of the friend to invite.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.cancelInvite", path = "party/{partyID}/invite/{friendID}", httpMethod = ApiMethod.HttpMethod.DELETE)
    public Party cancelInvite(@Named("partyID") final long partyID, @Named("friendID") final String friendID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                // Check if friend exists
                final User friend = getUserUnsafe(friendID);
                if (friend == null) {
                    throw new NotFoundException("Friend not found");
                }
                Party party = getParty(partyID, true);
                // User must be host
                if (!party.isHost(userID)) {
                    throw new UnauthorizedException("User must be party host to manage invites");
                }
                // Cancel invite
                try {
                    party.cancelInvite(friend);
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Save
                ofy().save().entities(party, friend).now();
                // Send cancel invite
                sendCancelInvite(party, friend);
                return party;
            }
        });
        return party;
    }

    protected void sendCancelInvite(Party party, User invitee) throws ServiceException {
        messageDAO.addMessages(Messages.partyInviteCanceled(party)
                        .invitee(invitee)
                        .recipients(invitee)
                        .build()
        );
    }

    /**
     * Accept an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.acceptInvite", path = "party/{partyID}/acceptInvite", httpMethod = ApiMethod.HttpMethod.POST)
    public Party acceptInvite(@Named("partyID") final long partyID, final TimeSlotCollection timeSlots, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                User user = getUser(userID);
                Party party = getParty(partyID, true);
                // Accept invite
                try {
                    party.acceptInvite(user, timeSlots.getList());
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Save
                ofy().save().entities(party, user).now();
                return party;
            }
        });
        // Send update to other recipients
        User user = getUser(userID);
        messageDAO.addMessages(Messages.partyUpdated(party)
                .reason(PartyUpdateReason.JOINED)
                .reasonUser(user)
                .recipients(party.getUpdateRecipientsExcept(User.getRef(userID)))
                .build());
        return party;
    }

    /**
     * Decline an invite to a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.declineInvite", path = "party/{partyID}/declineInvite", httpMethod = ApiMethod.HttpMethod.GET)
    public Party declineInvite(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                User user = getUser(userID);
                Party party = getParty(partyID, true);
                // Decline invite
                try {
                    party.declineInvite(user);
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Save
                ofy().save().entities(party, user).now();
                return party;
            }
        });
        // Send update to host
        User user = getUser(userID);
        messageDAO.addMessages(Messages.partyUpdated(party)
                .reason(PartyUpdateReason.DECLINED)
                .reasonUser(user)
                .recipients(party.getHost())
                .build());
        return party;
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
                // Leave the party
                try {
                    party.leave(user);
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Save
                ofy().save().entities(party, user).now();
                return party;
            }
        });
        // Send update to other recipients
        User user = getUser(userID);
        messageDAO.addMessages(Messages.partyUpdated(party)
                .reason(PartyUpdateReason.LEFT)
                .reasonUser(user)
                .recipients(party.getUpdateRecipientsExcept(User.getRef(userID)))
                .build());
    }

    /**
     * Retrieve candidates for a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     * @return The list of candidates.
     */
    @ApiMethod(name = "parties.getCandidates", path = "party/{partyID}/candidates", httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<PartyMember> getCandidates(@Named("partyID") long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        String userID = getUserID(accessToken);
        Party party = getParty(partyID, true);
        // User must be host
        if (!party.isHost(userID)) {
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
        return CollectionResponse.<PartyMember>builder().setItems(candidates).build();
    }

    /**
     * Close invites for a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.closeInvites", path = "party/{partyID}/closeInvites", httpMethod = ApiMethod.HttpMethod.GET)
    public Party closeInvites(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                Party party = getParty(partyID, true);
                // User must be host
                if (!party.isHost(userID)) {
                    throw new UnauthorizedException("User must be party host to close invites");
                }
                // Party must be inviting
                if (!party.isInviting()) {
                    throw new ConflictException("Party must be still inviting");
                }
                // Close invites
                try {
                    party.closeInvites();
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Save
                ofy().save().entity(party).now();
                return party;
            }
        });
        // Cancel open invites
        party = cancelInvites(party);
        return party;
    }

    /**
     * Plan a party.
     *
     * @param partyID     The party ID.
     * @param timeSlot    The chosen time slot.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.plan", path = "party/{partyID}/plan", httpMethod = ApiMethod.HttpMethod.POST)
    public Party plan(@Named("partyID") final long partyID, final TimeSlot timeSlot, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                Party party = getParty(partyID, true);
                // User must be host
                if (!party.isHost(userID)) {
                    throw new UnauthorizedException("User must be party host to plan");
                }
                // Plan the party
                try {
                    party.plan(timeSlot);
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Save
                ofy().save().entity(party).now();
                return party;
            }
        });
        // Send update to recipients except host
        messageDAO.addMessages(Messages.partyUpdated(party)
                .reason(PartyUpdateReason.DONE)
                .recipients(party.getUpdateRecipientsExceptHost())
                .build());
        return party;
    }


    /**
     * Disband a party.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "parties.disband", path = "party/{partyID}/disband", httpMethod = ApiMethod.HttpMethod.GET)
    public Party disband(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        Party party = transact(new Work<Party, ServiceException>() {
            @Override
            public Party run() throws ServiceException {
                Party party = getParty(partyID, true);
                User user = getUser(userID);
                // User must be host
                if (!party.isHost(user)) {
                    throw new UnauthorizedException("User must be party host to disband");
                }
                // Disband the party
                try {
                    party.disband();
                } catch (Exception e) {
                    throw new ConflictException(e.getMessage());
                }
                // Remove from party
                party.removeRecipient(user);
                // Save
                ofy().save().entities(party, user).now();
                return party;
            }
        });
        // Cancel open invites
        party = cancelInvites(party);
        // Send update to recipients except host
        messageDAO.addMessages(Messages.partyUpdated(party)
                .reason(PartyUpdateReason.DISBANDED)
                .recipients(party.getUpdateRecipientsExceptHost())
                .build());
        return party;
    }

    /**
     * Retrieves all parties for a user, most recent party first.
     *
     * @param accessToken The access token for authorization.
     * @return The parties of a user.
     */
    @ApiMethod(name = "users.getParties", path = "user/party", httpMethod = ApiMethod.HttpMethod.GET)
    public CollectionResponse<Party> getParties(@Named("accessToken") String accessToken) throws ServiceException {
        String userID = getUserID(accessToken);
        User user = getUser(userID);
        // Use reverse date ordering (recent dates first)
        Ordering<Party> dateOrdering = Party.dateComparator.reverse();
        // Sort user parties
        List<Party> parties = dateOrdering.immutableSortedCopy(user.getParties());
        return CollectionResponse.<Party>builder().setItems(parties).build();
    }

    /**
     * Removes a party from a user.
     *
     * @param partyID     The party ID.
     * @param accessToken The access token for authorization.
     */
    @ApiMethod(name = "users.removeParty", path = "user/party/{partyID}", httpMethod = ApiMethod.HttpMethod.DELETE)
    public void removeParty(@Named("partyID") final long partyID, @Named("accessToken") String accessToken) throws ServiceException {
        final String userID = getUserID(accessToken);
        transact(new TransactUtils.VoidWork<ServiceException>() {
            @Override
            public void vrun() throws ServiceException {
                Party party = getParty(partyID, true);
                User user = getUser(userID);
                if (!party.isCompleted() && !party.isDisbanded()) {
                    throw new ConflictException("Cannot remove party, must be completed or disbanded.");
                }
                // Remove from party
                party.removeRecipient(user);
                // Save
                ofy().save().entities(party, user).now();
            }
        });
    }

    /**
     * Cancel all open invites of a party.
     *
     * @param party The party.
     * @return The party after all invites are canceled, and the canceled invitees.
     * @throws ServiceException
     */
    protected Party cancelInvites(Party party) throws ServiceException {
        final long partyID = party.getID();
        // Keep finding invitees
        PartyMember invitee;
        while ((invitee = findInvitee(party)) != null) {
            final String inviteeID = invitee.getUserID();
            // Cancel each invite in a separate transaction
            party = transact(new Work<Party, ServiceException>() {
                @Override
                public Party run() throws ServiceException {
                    Party party = getParty(partyID, true);
                    User user = getUser(inviteeID);
                    // Cancel invite
                    try {
                        party.cancelInvite(user);
                    } catch (Exception e) {
                        throw new ConflictException(e.getMessage());
                    }
                    // Save
                    ofy().save().entities(party, user).now();
                    // Send cancel invite
                    sendCancelInvite(party, user);
                    return party;
                }
            });
        }
        return party;
    }

    private PartyMember findInvitee(Party party) {
        return Iterables.getFirst(party.getInvitees(), null);
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

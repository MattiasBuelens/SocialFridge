package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.appengine.repackaged.com.google.common.collect.Collections2;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Party.
 */
@Entity(name = Party.KIND)
public class Party {

    public static final String KIND = "Party";

    @Id
    private Long id;
    private User host;
    private List<User> partners = new ArrayList<>();
    private List<User> invitees = new ArrayList<>();

    public Party() {
    }

    public Party(long id, User host) {
        this.id = id;
        this.host = host;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Key getKey() {
        return getKey(getID());
    }

    public static Key getKey(long id) {
        return KeyFactory.createKey(KIND, id);
    }

    /**
     * Party ID.
     */
    public Long getID() {
        return id;
    }

    public static long getID(Key partyKey) {
        return partyKey.getId();
    }

    /**
     * Host.
     */
    public User getHost() {
        return host;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public String getHostID() {
        return getHost().getID();
    }

    /**
     * Partners.
     */
    public List<User> getPartners() {
        return partners;
    }

    public void setPartners(List<User> partners) {
        this.partners = partners;
    }

    public void addPartner(User partner) {
        // Add to partners
        List<User> partners = getPartners();
        if (!partners.contains(partner)) {
            partners.add(partner);
        }
        // Remove from invitees
        removeInvitee(partner);
    }

    public void removePartner(User partner) {
        getPartners().remove(partner);
    }

    /**
     * Invitees.
     */
    public List<User> getInvitees() {
        return invitees;
    }

    public void setInvitees(List<User> invitees) {
        this.invitees = invitees;
    }

    public void addInvitee(User invitee) {
        List<User> invitees = getInvitees();
        if (!invitees.contains(invitee)) {
            invitees.add(invitee);
        }
    }

    public void removeInvitee(User invitee) {
        getInvitees().remove(invitee);
    }

    /**
     * Members: host, partners and invitees.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public ImmutableList<User> getMembers() {
        return ImmutableList.<User>builder()
                .add(getHost())
                .addAll(getPartners())
                .addAll(getInvitees())
                .build();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Collection<String> getMemberIDs() {
        return Collections2.transform(getMembers(), new Function<User, String>() {
            @Override
            public String apply(User user) {
                return user.getID();
            }
        });
    }

}

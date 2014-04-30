package be.kuleuven.cs.chikwadraat.socialfridge;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.Work;

import be.kuleuven.cs.chikwadraat.socialfridge.model.User;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;


public class UserDAO {

    /**
     * Inserts or updates a user.
     *
     * @param user The user to be updated.
     * @return The updated user.
     */
    public User updateUser(final User user) {
        return ofy().transact(new Work<User>() {
            @Override
            public User run() {
                User storedUser = User.getRef(user.getID()).get();
                if (storedUser != null) {
                    // Manually copy to stored user
                    // We don't want to remove devices here, only add
                    storedUser.setName(user.getName());
                    storedUser.addDevices(user.getDevices());
                } else {
                    storedUser = user;
                }
                ofy().save().entity(storedUser).now();
                return storedUser;
            }
        });
    }

    /**
     * Removes a user.
     *
     * @param userRef A reference to the user.
     * @return The deleted user.
     */
    public User removeUser(final Ref<User> userRef) {
        return ofy().transact(new Work<User>() {
            @Override
            public User run() {
                User user = userRef.get();
                if (user != null) {
                    ofy().delete().entity(user).now();
                }
                return user;
            }
        });
    }

    public void registerUserDevice(final Ref<User> userRef, final String registrationID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                User user = userRef.get();
                if (user == null) return;
                user.addDevice(registrationID);
                ofy().save().entity(user).now();
            }
        });
    }

    public void registerUserDevice(final String userID, final String registrationID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                registerUserDevice(User.getRef(userID), registrationID);
            }
        });
    }

    public void unregisterUserDevice(final Ref<User> userRef, final String registrationID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                User user = userRef.get();
                if (user == null) return;
                user.removeDevice(registrationID);
                ofy().save().entity(user).now();
            }
        });
    }

    public void unregisterUserDevice(final String userID, final String registrationID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                unregisterUserDevice(User.getRef(userID), registrationID);
            }
        });
    }

    public void moveUserDevice(final Ref<User> userRef, final String oldRegID, final String newRegID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                User user = userRef.get();
                if (user == null) return;
                user.removeDevice(oldRegID);
                user.addDevice(newRegID);
                ofy().save().entity(user).now();
            }
        });
    }

    public void moveUserDevice(final String userID, final String oldRegID, final String newRegID) {
        ofy().transact(new com.googlecode.objectify.VoidWork() {
            @Override
            public void vrun() {
                moveUserDevice(User.getRef(userID), oldRegID, newRegID);
            }
        });
    }

}

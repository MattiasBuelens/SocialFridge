package be.kuleuven.cs.chikwadraat.socialfridge.auth;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookException;
import com.restfb.types.User;

import java.util.List;
import java.util.logging.Level;

import be.kuleuven.cs.chikwadraat.socialfridge.AppSettings;


/**
 * Facebook API.
 */
public class FacebookAPI {

    private static final int ACCESS_TO_USER_LIFETIME = 30; // in seconds

    private MemcacheService cache;

    public FacebookAPI() {
        cache = MemcacheServiceFactory.getMemcacheService();
        cache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    }

    protected FacebookClient getClient(String accessToken) throws FacebookException {
        return new DefaultFacebookClient(accessToken, AppSettings.getFacebookAppSecret());
    }

    protected FacebookClient getAppClient() throws FacebookException {
        return getClient(getAppAccessToken().getAccessToken());
    }

    /**
     * Get an app access token for our application.
     *
     * @return The app access token.
     * @throws FacebookException
     */
    protected FacebookClient.AccessToken getAppAccessToken() throws FacebookException {
        String tokenJson = (String) cache.get("fb:app-access-token");
        if (tokenJson == null) {
            // Get token
            FacebookClient.AccessToken token = rawGetAppAccessToken();
            // Cache token
            tokenJson = new DefaultJsonMapper().toJson(token);
            Expiration tokenExpiration = token.getExpires() == null ? null : Expiration.onDate(token.getExpires());
            cache.put("fb:app-access-token", tokenJson, tokenExpiration);
            return token;
        } else {
            return new DefaultJsonMapper().toJavaObject(tokenJson, FacebookClient.AccessToken.class);
        }
    }

    protected FacebookClient.AccessToken rawGetAppAccessToken() throws FacebookException {
        return new DefaultFacebookClient().obtainAppAccessToken(AppSettings.getFacebookAppID(), AppSettings.getFacebookAppSecret());
    }

    /**
     * Gets the user ID authenticated by the given access token.
     *
     * @param accessToken The user access token.
     * @return The user ID.
     * @throws FacebookException
     */
    public String getUserID(String accessToken) throws FacebookException {
        Preconditions.checkNotNull(accessToken);
        String userID = (String) cache.get("fb:access-token-to-user:" + accessToken);
        if (userID == null) {
            // Get user ID
            userID = rawGetUserID(accessToken);
            // Cache user ID
            cache.put("fb:access-token-to-user:" + accessToken, userID, Expiration.byDeltaSeconds(ACCESS_TO_USER_LIFETIME));
        }
        return userID;
    }

    protected String rawGetUserID(String accessToken) throws FacebookException {
        FacebookClient client = getAppClient();
        FacebookClient.DebugTokenInfo info = client.debugToken(accessToken);
        return info.getUserId();
    }

    /**
     * Gets the friends of the user authenticated by the given access token.
     *
     * @param accessToken The user access token.
     * @return The list of friends.
     * @throws FacebookException
     */
    public Iterable<User> getFriends(String accessToken) throws FacebookException {
        Preconditions.checkNotNull(accessToken);
        FacebookClient client = getClient(accessToken);
        Connection<User> friends = client.fetchConnection("me/friends", User.class);
        return Iterables.concat(friends);
    }

    /**
     * Gets the friends of the user authenticated by the given access token.
     *
     * @param friendID    The friend's user ID.
     * @param accessToken The user access token.
     * @return The list of friends.
     * @throws FacebookException
     */
    public boolean isBefriendedWith(String friendID, String accessToken) throws FacebookException {
        Preconditions.checkNotNull(friendID);
        Preconditions.checkNotNull(accessToken);
        FacebookClient client = getClient(accessToken);
        Connection<User> friends = client.fetchConnection("me/friends/" + friendID, User.class, Parameter.with("fields", "id"));
        return !friends.getData().isEmpty();
    }

}

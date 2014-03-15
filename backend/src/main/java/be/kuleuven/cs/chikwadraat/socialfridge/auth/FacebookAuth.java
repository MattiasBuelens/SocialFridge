package be.kuleuven.cs.chikwadraat.socialfridge.auth;

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Preconditions;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;

import java.util.Date;
import java.util.logging.Level;

import be.kuleuven.cs.chikwadraat.socialfridge.AppSettings;


/**
 * Facebook authentication.
 */
public class FacebookAuth {

    private static final int ACCESS_TO_USER_LIFETIME = 30; // in seconds

    private MemcacheService cache;

    public FacebookAuth() {
        cache = MemcacheServiceFactory.getMemcacheService();
        cache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
    }

    /**
     * Gets the user ID authenticated by the given access token.
     *
     * @param accessToken The user access token.
     * @return The user ID.
     * @throws FacebookException
     */
    public String getUserId(String accessToken) throws FacebookException {
        Preconditions.checkNotNull(accessToken);
        String userId = (String) cache.get("fb:access-token-to-user:" + accessToken);
        if (userId == null) {
            // Get user ID
            userId = rawGetUserId(accessToken);
            // Cache user ID
            cache.put("fb:access-token-to-user:" + accessToken, userId, Expiration.byDeltaSeconds(ACCESS_TO_USER_LIFETIME));
        }
        return userId;
    }

    protected String rawGetUserId(String accessToken) throws FacebookException {
        FacebookClient client = getAppClient();
        FacebookClient.DebugTokenInfo info = client.debugToken(accessToken);
        return info.getUserId();
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
            // Cache expires one minute earlier
            Date expireDate = new Date(token.getExpires().getTime() - 60 * 1000);
            cache.put("fb:app-access-token", tokenJson, Expiration.onDate(expireDate));
            return token;
        } else {
            return new DefaultJsonMapper().toJavaObject(tokenJson, FacebookClient.AccessToken.class);
        }
    }

    protected FacebookClient.AccessToken rawGetAppAccessToken() throws FacebookException {
        return new DefaultFacebookClient().obtainAppAccessToken(AppSettings.getFacebookAppId(), AppSettings.getFacebookAppSecret());
    }

}

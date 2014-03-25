package be.kuleuven.cs.chikwadraat.socialfridge.auth;

import com.google.api.server.spi.response.UnauthorizedException;
import com.restfb.exception.FacebookException;

/**
 * Base class for endpoints requiring authentication.
 */
public abstract class FacebookAuthEndpoint extends AuthEndpoint {

    private final FacebookAPI api;

    protected FacebookAuthEndpoint() {
        this.api = new FacebookAPI();
    }

    protected FacebookAPI getAPI() {
        return api;
    }

    @Override
    protected String getUserID(String accessToken) throws UnauthorizedException {
        try {
            return getAPI().getUserID(accessToken);
        } catch (FacebookException e) {
            throw new UnauthorizedException(e);
        }
    }

}

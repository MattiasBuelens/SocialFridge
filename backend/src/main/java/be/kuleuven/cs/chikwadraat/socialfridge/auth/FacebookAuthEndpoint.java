package be.kuleuven.cs.chikwadraat.socialfridge.auth;

import com.google.api.server.spi.response.UnauthorizedException;
import com.restfb.exception.FacebookException;

/**
 * Base class for endpoints requiring authentication.
 */
public abstract class FacebookAuthEndpoint extends AuthEndpoint {

    private final FacebookAuth auth;

    protected FacebookAuthEndpoint() {
        this.auth = new FacebookAuth();
    }

    protected void checkAccess(String accessToken) throws UnauthorizedException {
        try {
            auth.getUserId(accessToken);
        } catch (FacebookException e) {
            throw new UnauthorizedException(e);
        }
    }

    protected void checkAccess(String accessToken, String userID) throws UnauthorizedException {
        String tokenID = auth.getUserId(accessToken);
        if (!tokenID.equals(userID)) {
            throw new UnauthorizedException("Incorrect permission for this user.");
        }
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.auth;

import com.google.api.server.spi.response.UnauthorizedException;

import java.util.Arrays;
import java.util.Collection;

/**
 * Interface for endpoints requiring authentication.
 */
public abstract class AuthEndpoint {

    /**
     * Get the user ID owning the given access token
     *
     * @param accessToken The access token to check.
     * @return The owner's user ID.
     * @throws UnauthorizedException
     */
    protected abstract String getUserID(String accessToken) throws UnauthorizedException;

    /**
     * Check whether the given access token is valid.
     *
     * @param accessToken The access token to check.
     * @throws UnauthorizedException
     */
    protected void checkAccess(String accessToken) throws UnauthorizedException {
        getUserID(accessToken);
    }

    /**
     * Check whether the given access token is valid for any of the given users.
     *
     * @param accessToken    The access token to check.
     * @param allowedUserIDs The IDs of allowed users.
     * @throws UnauthorizedException
     */
    protected void checkAccess(String accessToken, String... allowedUserIDs) throws UnauthorizedException {
        checkAccess(accessToken, Arrays.asList(allowedUserIDs));
    }

    /**
     * Check whether the given access token is valid for any of the given users.
     *
     * @param accessToken    The access token to check.
     * @param allowedUserIDs The IDs of allowed users.
     * @throws UnauthorizedException
     */
    protected void checkAccess(String accessToken, Collection<String> allowedUserIDs) throws UnauthorizedException {
        String tokenID = getUserID(accessToken);
        if (!allowedUserIDs.contains(tokenID)) {
            throw new UnauthorizedException("Incorrect permission for this user.");
        }
    }

}

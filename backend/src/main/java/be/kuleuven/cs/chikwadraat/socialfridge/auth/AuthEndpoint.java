package be.kuleuven.cs.chikwadraat.socialfridge.auth;

import com.google.api.server.spi.response.UnauthorizedException;

/**
 * Interface for endpoints requiring authentication.
 */
public abstract class AuthEndpoint {

    /**
     * Check whether the given access token is valid.
     *
     * @param accessToken The access token to check.
     * @throws UnauthorizedException
     */
    protected abstract void checkAccess(String accessToken) throws UnauthorizedException;

    /**
     * Check whether the given access token is valid for the given user.
     *
     * @param accessToken The access token to check.
     * @param userID      The expected user ID.
     * @throws UnauthorizedException
     */
    protected abstract void checkAccess(String accessToken, String userID) throws UnauthorizedException;

}

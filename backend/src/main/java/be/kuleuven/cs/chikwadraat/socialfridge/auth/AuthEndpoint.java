package be.kuleuven.cs.chikwadraat.socialfridge.auth;

import be.kuleuven.cs.chikwadraat.socialfridge.auth.FacebookAuth;

/**
 * Interface for endpoints requiring authentication.
 */
public abstract class AuthEndpoint {

    /**
     * Check whether the given access token is valid.
     *
     * @param accessToken The access token to check.
     * @throws AuthException
     */
    protected abstract void checkAccess(String accessToken) throws AuthException;

    /**
     * Check whether the given access token is valid for the given user.
     *
     * @param accessToken The access token to check.
     * @param userID      The expected user ID.
     * @throws AuthException
     */
    protected abstract void checkAccess(String accessToken, String userID) throws AuthException;

}

package be.kuleuven.cs.chikwadraat.socialfridge.auth;

/**
 * Authentication exception.
 */
public class AuthException extends Exception {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }

}

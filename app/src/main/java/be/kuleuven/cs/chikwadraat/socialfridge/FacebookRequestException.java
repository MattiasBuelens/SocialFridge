package be.kuleuven.cs.chikwadraat.socialfridge;

import com.facebook.FacebookRequestError;

/**
 * Created by Mattias on 7/04/2014.
 */
public class FacebookRequestException extends Exception {

    private final FacebookRequestError error;

    public FacebookRequestException(FacebookRequestError error) {
        super(error.getErrorMessage());
        this.error = error;
    }

    public FacebookRequestException(FacebookRequestError error, Throwable throwable) {
        super(error.getErrorMessage(), throwable);
        this.error = error;
    }

    public FacebookRequestError getError() {
        return error;
    }

}

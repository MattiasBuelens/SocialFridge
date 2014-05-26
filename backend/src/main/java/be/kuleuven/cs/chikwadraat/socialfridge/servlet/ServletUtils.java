package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by Mattias on 26/05/2014.
 */
public class ServletUtils {

    private ServletUtils() {
    }

    public static void disableCaching(HttpServletResponse resp) {
        // Set to expire far in the past.
        resp.setHeader("Expires", "Thu, 1 Jan 1970 00:00:00 GMT");

        // Set standard HTTP/1.1 no-cache headers.
        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        resp.addHeader("Cache-Control", "post-check=0, pre-check=0");

        // Set standard HTTP/1.0 no-cache header.
        resp.setHeader("Pragma", "no-cache");
    }

}

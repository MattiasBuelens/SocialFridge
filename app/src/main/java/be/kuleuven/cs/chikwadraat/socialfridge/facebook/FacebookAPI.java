package be.kuleuven.cs.chikwadraat.socialfridge.facebook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;

import java.io.IOException;

/**
 * Created by Mattias on 17/04/2014.
 */
public class FacebookAPI {

    public static Bitmap getProfilePicture(String profileId, Preset preset) throws IOException {
        GenericUrl pictureURL = new GenericUrl("http://graph.facebook.com/" + profileId + "/picture?type=" + preset.getValue());
        return getBitmap(pictureURL);
    }

    public static Bitmap getProfilePicture(String profileId, int width, int height) throws IOException {
        GenericUrl pictureURL = new GenericUrl("http://graph.facebook.com/" + profileId + "/picture?width=" + width + "&height=" + height);
        return getBitmap(pictureURL);
    }

    private static Bitmap getBitmap(GenericUrl url) throws IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        HttpRequest request = transport.createRequestFactory().buildGetRequest(url);
        request.setFollowRedirects(true);

        HttpResponse response = request.execute();
        return BitmapFactory.decodeStream(response.getContent());
    }

    public enum Preset {
        SQUARE("square"), SMALL("small"), NORMAL("normal"), LARGE("large");

        private final String value;

        Preset(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}

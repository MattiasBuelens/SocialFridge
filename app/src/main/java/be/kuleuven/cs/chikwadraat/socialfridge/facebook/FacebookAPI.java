package be.kuleuven.cs.chikwadraat.socialfridge.facebook;

import android.graphics.Bitmap;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.common.io.ByteStreams;

import java.io.IOException;

import javax.annotation.Nullable;

import be.kuleuven.cs.chikwadraat.socialfridge.util.BitmapUtils;

/**
 * Created by Mattias on 17/04/2014.
 */
public class FacebookAPI {

    public Bitmap getProfilePicture(String profileId, Preset preset) throws IOException {
        GenericUrl url = getProfilePictureUrl(profileId, preset);
        return getBitmap(url, 0, 0);
    }

    public GenericUrl getProfilePictureUrl(String profileId, Preset preset) {
        return new GenericUrl("http://graph.facebook.com/" + profileId + "/picture?type=" + preset.getValue());
    }

    public Bitmap getProfilePicture(String profileId, @Nullable Integer maxWidth, @Nullable Integer maxHeight) throws IOException {
        GenericUrl url = getProfilePictureUrl(profileId, maxWidth, maxHeight);
        return getBitmap(url, (maxWidth == null ? 0 : maxWidth), (maxHeight == null ? 0 : maxHeight));
    }

    public GenericUrl getProfilePictureUrl(String profileId, @Nullable Integer width, @Nullable Integer height) {
        StringBuilder url = new StringBuilder("http://graph.facebook.com/" + profileId + "/picture");
        if (width != null) {
            url.append("?width=").append(width);
            if (height != null) {
                url.append("&height=").append(height);
            }
        } else if (height != null) {
            url.append("?height=").append(height);
        }
        return new GenericUrl(url.toString());
    }

    protected Bitmap getBitmap(GenericUrl url, int maxWidth, int maxHeight) throws IOException {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        HttpRequest request = transport.createRequestFactory().buildGetRequest(url);
        request.setFollowRedirects(true);
        HttpResponse response = request.execute();

        byte[] bitmapData = ByteStreams.toByteArray(response.getContent());
        return BitmapUtils.createBitmap(bitmapData, maxWidth, maxHeight);
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

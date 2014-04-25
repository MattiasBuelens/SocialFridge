package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.InputSettings;
import com.google.appengine.api.images.OutputSettings;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.images.Transform;

import java.util.Collection;
import java.util.concurrent.Future;

import be.kuleuven.cs.chikwadraat.socialfridge.util.NetworkUtils;

/**
 * Created by Mattias on 22/04/2014.
 */
public class ImagesService implements com.google.appengine.api.images.ImagesService {

    private static final ImagesService instance = new ImagesService();

    private final com.google.appengine.api.images.ImagesService delegate;

    private ImagesService() {
        delegate = ImagesServiceFactory.getImagesService();
    }

    public static ImagesService get() {
        return instance;
    }

    /**
     * When running on the local development server, this replaces the "any local" IP address
     * (typically 0.0.0.0) in the serving URL with the local host's IP address.
     *
     * @param url The serving URL.
     * @return The fixed serving URL.
     */
    protected String fixServiceUrl(String url) {
        if (Application.isDevelopment()) {
            String hostAddress = NetworkUtils.getPublicAddress().getHostAddress();
            url = url.replace("://0.0.0.0", "://" + hostAddress);
        }
        return url;
    }

    @Override
    public String getServingUrl(ServingUrlOptions servingUrlOptions) {
        return fixServiceUrl(delegate.getServingUrl(servingUrlOptions));
    }

    @Override
    @Deprecated
    public String getServingUrl(BlobKey blobKey, int i, boolean b, boolean b2) {
        return fixServiceUrl(delegate.getServingUrl(blobKey, i, b, b2));
    }

    @Override
    @Deprecated
    public String getServingUrl(BlobKey blobKey, int i, boolean b) {
        return fixServiceUrl(delegate.getServingUrl(blobKey, i, b));
    }

    @Override
    @Deprecated
    public String getServingUrl(BlobKey blobKey, boolean b) {
        return fixServiceUrl(delegate.getServingUrl(blobKey, b));
    }

    @Override
    @Deprecated
    public String getServingUrl(BlobKey blobKey) {
        return fixServiceUrl(delegate.getServingUrl(blobKey));
    }

    @Override
    public void deleteServingUrl(BlobKey blobKey) {
        delegate.deleteServingUrl(blobKey);
    }

    @Override
    public Image applyTransform(Transform transform, Image image) {
        return delegate.applyTransform(transform, image);
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image) {
        return delegate.applyTransformAsync(transform, image);
    }

    @Override
    public Image applyTransform(Transform transform, Image image, OutputEncoding outputEncoding) {
        return delegate.applyTransform(transform, image, outputEncoding);
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image, OutputEncoding outputEncoding) {
        return delegate.applyTransformAsync(transform, image, outputEncoding);
    }

    @Override
    public Image applyTransform(Transform transform, Image image, OutputSettings outputSettings) {
        return delegate.applyTransform(transform, image, outputSettings);
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image, OutputSettings outputSettings) {
        return delegate.applyTransformAsync(transform, image, outputSettings);
    }

    @Override
    public Image applyTransform(Transform transform, Image image, InputSettings inputSettings, OutputSettings outputSettings) {
        return delegate.applyTransform(transform, image, inputSettings, outputSettings);
    }

    @Override
    public Future<Image> applyTransformAsync(Transform transform, Image image, InputSettings inputSettings, OutputSettings outputSettings) {
        return delegate.applyTransformAsync(transform, image, inputSettings, outputSettings);
    }

    @Override
    public Image composite(Collection<Composite> composites, int i, int i2, long l) {
        return delegate.composite(composites, i, i2, l);
    }

    @Override
    public Image composite(Collection<Composite> composites, int i, int i2, long l, OutputEncoding outputEncoding) {
        return delegate.composite(composites, i, i2, l, outputEncoding);
    }

    @Override
    public Image composite(Collection<Composite> composites, int i, int i2, long l, OutputSettings outputSettings) {
        return delegate.composite(composites, i, i2, l, outputSettings);
    }

    @Override
    public int[][] histogram(Image image) {
        return delegate.histogram(image);
    }

}

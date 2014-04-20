package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.io.Serializable;

/**
 * Dish.
 */
@Entity(name = Dish.KIND)
public class Dish {

    public static final int THUMBNAIL_SIZE = 256;

    public static final String KIND = "Dish";

    @Id
    private Long id;

    /*
     * Dish name.
     */
    @Index
    private String name;

    /**
     * Blob key of dish picture.
     */
    private BlobKey pictureKey;

    public Dish() {
    }

    public Dish(long id) {
        this.id = id;
    }

    public static Key<Dish> getKey(long dishID) {
        return Key.create(Dish.class, dishID);
    }

    public static Ref<Dish> getRef(long dishID) {
        return Ref.create(getKey(dishID));
    }

    /**
     * User ID.
     */
    public Long getID() {
        return id;
    }

    /**
     * Dish name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Blob key of dish picture.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public BlobKey getPictureKey() {
        return pictureKey;
    }

    public void setPictureKey(BlobKey pictureKey) {
        this.pictureKey = pictureKey;
    }

    /**
     * URL for dish picture.
     */
    public String getPictureURL() {
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(getPictureKey());
        return ImagesServiceFactory.getImagesService().getServingUrl(options);
    }

    public String getPictureURL(int imageSize) {
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(getPictureKey())
                .imageSize(imageSize);
        return ImagesServiceFactory.getImagesService().getServingUrl(options);
    }

    public String getPictureURL(int imageSize, boolean crop) {
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(getPictureKey())
                .imageSize(imageSize)
                .crop(crop);
        return ImagesServiceFactory.getImagesService().getServingUrl(options);
    }

    /**
     * URL for dish thumbnail.
     */
    public String getThumbnailURL() {
        return getPictureURL(THUMBNAIL_SIZE, true);
    }

}

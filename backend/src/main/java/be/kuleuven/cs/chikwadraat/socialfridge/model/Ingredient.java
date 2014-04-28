package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.ServingUrlOptions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import be.kuleuven.cs.chikwadraat.socialfridge.ImagesService;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Quantity;

/**
 * Created by Milan Samyn on 23/04/2014.
 */
@Entity(name = Ingredient.KIND)
public class Ingredient {

    public static final int THUMBNAIL_SIZE = 256;

    public static final String KIND = "Ingredient";

    @Id
    private Long id;

    /*
     * Ingredient name.
     */
    @Index
    private String name;

    /**
     * Ingredient category.
     */
    @Index
    private Category category;

    /**
     * Ingredient standard quantity
     */
    @Index
    private Quantity standardQuantity;

    @Index
    private Double amount;

    @Index
    private Double defaultAmount;

    /**
     * Blob key of ingredient picture.
     */
    private BlobKey pictureKey;

    public Ingredient() {
    }

    public Ingredient(long id) {
        this.id = id;
    }

    public static Key<Ingredient> getKey(long ingredientID) {
        return Key.create(Ingredient.class, ingredientID);
    }

    public static Ref<Ingredient> getRef(long ingredientID) {
        return Ref.create(getKey(ingredientID));
    }

    /**
     * User ID.
     */
    public Long getID() {
        return id;
    }

    /**
     * Ingredient name.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Ingredient category.
     */
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Ingredient standard quantity
     */
    public Quantity getStandardQuantity() {
        return standardQuantity;
    }

    public void setStandardQuantity(Quantity standardQuantity) {
        this.standardQuantity = standardQuantity;
    }

    /**
     * Ingredient amount
     */
    public Double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Ingredient default amount
     */
    public Double getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(double defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    /**
     * Blob key of ingredient picture.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public BlobKey getPictureKey() {
        return pictureKey;
    }

    public void setPictureKey(BlobKey pictureKey) {
        this.pictureKey = pictureKey;
    }

    /**
     * URL for ingredient picture.
     */
    public String getPictureURL() {
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(getPictureKey());
        return ImagesService.get().getServingUrl(options);
    }

    public String getPictureURL(int imageSize) {
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(getPictureKey())
                .imageSize(imageSize);
        return ImagesService.get().getServingUrl(options);
    }

    public String getPictureURL(int imageSize, boolean crop) {
        ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(getPictureKey())
                .imageSize(imageSize)
                .crop(crop);
        return ImagesService.get().getServingUrl(options);
    }

    /**
     * URL for ingredient thumbnail.
     */
    public String getThumbnailURL() {
        return getPictureURL(THUMBNAIL_SIZE, true);
    }


    public static enum Category {

        FATS,

        DAIRY,

        MEAT,

        VEGETABLES,

        CEREALS,

        FISH,

        FRUIT;

    }



}

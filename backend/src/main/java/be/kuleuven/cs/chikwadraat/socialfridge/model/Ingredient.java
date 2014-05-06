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
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Quantity;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

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
     * Category.
     */
    @Index
    private Category category;

    /**
     * Default amount.
     */
    private double defaultAmount;

    /**
     * Default unit.
     */
    private Unit defaultUnit;

    /**
     * Blob key of ingredient picture.
     */
    private BlobKey pictureKey;

    public Ingredient() {
    }

    public Ingredient(long id) {
        this.id = id;
    }

    public Ingredient(String name, Category category, Measure defaultMeasure) {
        setName(name);
        setCategory(category);
        setDefaultMeasure(defaultMeasure);
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
     * Category.
     */
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Default amount.
     */
    public Double getDefaultAmount() {
        return defaultAmount;
    }

    public void setDefaultAmount(double defaultAmount) {
        this.defaultAmount = defaultAmount;
    }

    /**
     * Default unit.
     */
    public Unit getDefaultUnit() {
        return defaultUnit;
    }

    public void setDefaultUnit(Unit defaultUnit) {
        this.defaultUnit = defaultUnit;
    }

    /**
     * Get the quantity to express measures of this ingredient.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Quantity getQuantity() {
        return getDefaultUnit().getQuantity();
    }

    /**
     * Get the default measure when creating a fridge item for this ingredient.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Measure getDefaultMeasure() {
        return new Measure(getDefaultAmount(), getDefaultUnit());
    }

    /**
     * Set the default measure when creating a fridge item for this ingredient.
     */
    public void setDefaultMeasure(Measure defaultMeasure) {
        setDefaultAmount(defaultMeasure.getValue());
        setDefaultUnit(defaultMeasure.getUnit());
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

        FATS("Fats"),

        DAIRY("Dairy"),

        MEAT("Meat"),

        VEGETABLES("Vegetables"),

        CEREALS("Cereals"),

        FISH("Fish"),

        FRUIT("Fruit");

        private final String label;

        private Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        /**
         * For JSTL.
         */
        public String getName() {
            return name();
        }
    }


}

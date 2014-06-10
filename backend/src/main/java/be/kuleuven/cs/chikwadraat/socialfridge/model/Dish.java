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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import be.kuleuven.cs.chikwadraat.socialfridge.ImagesService;

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

    /**
     * Items, per person.
     */
    private List<DishItem> items = new ArrayList<DishItem>();

    public Dish() {
    }

    public Dish(long id) {
        this.id = id;
    }

    public Dish(String name) {
        setName(name);
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
     * URL for dish thumbnail.
     */
    public String getThumbnailURL() {
        return getPictureURL(THUMBNAIL_SIZE, true);
    }

    /**
     * Items, per person.
     */
    public List<DishItem> getItems() {
        return items;
    }

    public void setItems(List<DishItem> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    /**
     * Ingredients, per person.
     */
    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Set<Ref<Ingredient>> getIngredientRefs() {
        Set<Ref<Ingredient>> refs = new HashSet<Ref<Ingredient>>();
        for (DishItem item : getItems()) {
            refs.add(item.getIngredientRef());
        }
        return refs;
    }

    /**
     * Match the given fridge items with the ingredients of this dish.
     *
     * @param fridgeItems The fridge items.
     * @return The matching fridge items, as dish items.
     */
    public List<DishItem> matchFridge(Collection<FridgeItem> fridgeItems) {
        List<DishItem> dishItems = new ArrayList<DishItem>();
        Set<Ref<Ingredient>> ingredients = getIngredientRefs();
        for (FridgeItem fridgeItem : fridgeItems) {
            if (ingredients.contains(fridgeItem.getIngredientRef())) {
                dishItems.add(DishItem.fromFridge(fridgeItem));
            }
        }
        return dishItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish that = (Dish) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.model;

/**
 * Created by vital.dhaveloose on 24/03/2014.
 */
public class Dish {

    private String name;
    private String description;
    private int imageResource;

    public Dish(String name, String description, int imageResource) {
        this.name = name;
        this.description = description;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

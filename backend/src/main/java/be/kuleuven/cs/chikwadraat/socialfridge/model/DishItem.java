package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Load;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * Created by Mattias on 19/05/2014.
 */
@Embed
public class DishItem {

    @Load
    private Ref<Ingredient> ingredient;

    private double standardAmount;

    public DishItem() {
    }

    public DishItem(Ref<Ingredient> ingredient) {
        this.ingredient = ingredient;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Ref<Ingredient> getIngredientRef() {
        return ingredient;
    }

    public void setIngredientRef(Ref<Ingredient> ingredient) {
        this.ingredient = ingredient;
    }

    public Ingredient getIngredient() {
        return getIngredientRef().get();
    }

    public void setIngredient(Ingredient ingredient) {
        setIngredientRef(Ref.create(ingredient));
    }

    public double getStandardAmount() {
        return standardAmount;
    }

    public void setStandardAmount(double standardAmount) {
        this.standardAmount = standardAmount;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Unit getStandardUnit() {
        return getIngredient().getQuantity().getStandardUnit();
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Measure getMeasure() {
        return new Measure(getStandardAmount(), getStandardUnit());
    }

    public void setMeasure(Measure measure) {
        setStandardAmount(measure.getValue(getStandardUnit()));
    }

}

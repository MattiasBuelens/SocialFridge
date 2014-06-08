package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
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

    public DishItem copy() {
        DishItem copy = new DishItem(getIngredientRef());
        copy.setStandardAmount(getStandardAmount());
        return copy;
    }

    public void add(double standardAmount) {
        setStandardAmount(standardAmount + getStandardAmount());
    }

    public void subtract(double standardAmount) {
        add(-standardAmount);
    }

    public void multiply(int scale) {
        setStandardAmount(scale * getStandardAmount());
    }

    public DishItem plus(double standardAmount) {
        DishItem copy = copy();
        copy.add(standardAmount);
        return copy;
    }

    public DishItem minus(double standardAmount) {
        return plus(-standardAmount);
    }

    public DishItem times(int scale) {
        DishItem copy = copy();
        copy.multiply(scale);
        return copy;
    }

    public static DishItem fromFridge(FridgeItem fridgeItem) {
        DishItem dishItem = new DishItem(fridgeItem.getIngredientRef());
        dishItem.setMeasure(fridgeItem.getMeasure());
        return dishItem;
    }

    public static final Ordering<DishItem> amountComparator = new Ordering<DishItem>() {
        @Override
        public int compare(DishItem left, DishItem right) {
            return Doubles.compare(left.getStandardAmount(), right.getStandardAmount());
        }
    };

}

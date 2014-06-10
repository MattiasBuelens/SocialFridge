package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.collect.Ordering;
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
        copy.setMeasure(getMeasure());
        return copy;
    }

    public void add(Measure measure) {
        setMeasure(getMeasure().plus(measure));
    }

    public void add(double standardAmount) {
        add(new Measure(standardAmount, getStandardUnit()));
    }

    public void subtract(Measure measure) {
        setMeasure(getMeasure().plus(measure));
    }

    public void subtract(double standardAmount) {
        subtract(new Measure(standardAmount, getStandardUnit()));
    }

    public void multiply(long scale) {
        setMeasure(getMeasure().times(scale));
    }

    public DishItem plus(Measure measure) {
        DishItem copy = copy();
        copy.add(measure);
        return copy;
    }

    public DishItem plus(double standardAmount) {
        DishItem copy = copy();
        copy.add(standardAmount);
        return copy;
    }

    public DishItem minus(Measure measure) {
        return plus(measure.negate());
    }

    public DishItem minus(double standardAmount) {
        return plus(-standardAmount);
    }

    public DishItem times(long scale) {
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
            return left.getMeasure().compareTo(right.getMeasure());
        }
    };

}

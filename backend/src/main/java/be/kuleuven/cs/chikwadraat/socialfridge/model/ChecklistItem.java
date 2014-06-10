package be.kuleuven.cs.chikwadraat.socialfridge.model;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;

import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;

/**
 * Created by Mattias on 19/05/2014.
 */
public class ChecklistItem {

    private Ingredient ingredient;
    private Measure bringAmount;
    private Measure requiredAmount;

    public ChecklistItem() {
    }

    public ChecklistItem(Ingredient ingredient) {
        this.ingredient = ingredient;
        setBringAmount(0d);
        setRequiredAmount(0d);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Unit getStandardUnit() {
        return getIngredient().getQuantity().getStandardUnit();
    }

    public double getBringAmount() {
        return getBringMeasure().getValue(getStandardUnit());
    }

    public void setBringAmount(double bringAmount) {
        setBringMeasure(new Measure(bringAmount, getStandardUnit()));
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Measure getBringMeasure() {
        return bringAmount;
    }

    public void setBringMeasure(Measure bringAmount) {
        this.bringAmount = bringAmount;
    }

    public double getRequiredAmount() {
        return getRequiredMeasure().getValue(getStandardUnit());
    }

    public void setRequiredAmount(double requiredAmount) {
        setRequiredMeasure(new Measure(requiredAmount, getStandardUnit()));
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Measure getRequiredMeasure() {
        return requiredAmount;
    }

    public void setRequiredMeasure(Measure requiredAmount) {
        this.requiredAmount = requiredAmount;
    }

    public double getMissingAmount() {
        return Math.min(0, getRequiredAmount() - getBringAmount());
    }

    @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
    public Measure getMissingMeasure() {
        return new Measure(getMissingAmount(), getStandardUnit());
    }

}

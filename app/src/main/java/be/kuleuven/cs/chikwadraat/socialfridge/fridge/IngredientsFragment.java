package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.Activity;

import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;

/**
 * Fragment displaying a list of ingredients to add to a user's fridge.
 * Use the {@link IngredientsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IngredientsFragment extends AbstractFridgeFragment<Ingredient> {

    private static final String TAG = "IngredientsFragment";

    private IngredientsListener listener;

    /**
     * Create a new ingredients fragment.
     *
     * @return A new instance of fragment IngredientsFragment.
     */
    public static IngredientsFragment newInstance() {
        IngredientsFragment fragment = new IngredientsFragment();
        return fragment;
    }

    public IngredientsFragment() {
        // Required empty public constructor
    }

    @Override
    public Ingredient getIngredient(Ingredient ingredient) {
        return ingredient;
    }

    @Override
    public boolean showMeasure(Ingredient ingredient) {
        return false;
    }

    @Override
    public Measure getMeasure(Ingredient ingredient) {
        return ingredient.getDefaultMeasure();
    }

    @Override
    public boolean allowRemove(Ingredient ingredient) {
        return false;
    }

    @Override
    public void onItemUpdated(Ingredient ingredient, Measure measure) {
        // TODO Create FridgeItem
        FridgeItem item = new FridgeItem(ingredient, measure);
        if (listener != null) {
            listener.onFridgeItemAdded(item);
        }
    }

    @Override
    public void onItemRemoved(Ingredient ingredient) {
        // Cannot happen
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (IngredientsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IngredientsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface IngredientsListener {

        public void onFridgeItemAdded(FridgeItem item);

    }

}

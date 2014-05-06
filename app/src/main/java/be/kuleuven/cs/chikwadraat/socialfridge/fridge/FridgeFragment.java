package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.app.Activity;

import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;

/**
 * Fragment displaying items in a user's fridge.
 * Use the {@link be.kuleuven.cs.chikwadraat.socialfridge.fridge.FridgeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FridgeFragment extends AbstractFridgeFragment<FridgeItem> {

    private static final String TAG = "IngredientsFragment";

    private FridgeListener listener;

    /**
     * Create a new fridge fragment.
     *
     * @return A new instance of fragment FridgeItem.
     */
    public static FridgeFragment newInstance() {
        FridgeFragment fragment = new FridgeFragment();
        return fragment;
    }

    public FridgeFragment() {
        // Required empty public constructor
    }

    @Override
    public Ingredient getIngredient(FridgeItem item) {
        // TODO return item.getIngredient();
        return null;
    }

    @Override
    public boolean showMeasure(FridgeItem item) {
        return true;
    }

    @Override
    public Measure getMeasure(FridgeItem item) {
        return item.getMeasure();
    }

    @Override
    public boolean allowRemove(FridgeItem item) {
        return true;
    }

    @Override
    public void onItemUpdated(FridgeItem item, Measure measure) {
        item.setMeasure(measure);
        if (listener != null) {
            listener.onFridgeItemUpdated(item);
        }
    }

    @Override
    public void onItemRemoved(FridgeItem item) {
        if (listener != null) {
            listener.onFridgeItemRemoved(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (FridgeListener) activity;
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

    public interface FridgeListener {

        public void onFridgeItemUpdated(FridgeItem item);

        public void onFridgeItemRemoved(FridgeItem item);

    }

}

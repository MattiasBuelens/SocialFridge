package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Manage fridge activity.
 */
public class FridgeActivity extends BaseActivity implements ObservableAsyncTask.Listener<Void, Void>, View.OnClickListener, FridgeFragment.FridgeListener, IngredientsFragment.IngredientsListener {

    private static final String TAG = "FridgeActivity";

    private FridgeFragment fridgeFragment;
    private IngredientsFragment ingredientsFragment;
    private Button addIngredientsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge_my_fridge);

        fridgeFragment = (FridgeFragment) getSupportFragmentManager().findFragmentById(R.id.fridge_fragment);
        ingredientsFragment = (IngredientsFragment) getSupportFragmentManager().findFragmentById(R.id.ingredients_fragment);
        addIngredientsButton = (Button) findViewById(R.id.fridge_action_add_ingredients);

        addIngredientsButton.setOnClickListener(this);

        //TODO Re-attach to close invites task ?

        // TODO Remove dummy items
        List<FridgeItem> items = new ArrayList<FridgeItem>();
        items.add(new FridgeItem("John", new Ingredient("Eggs", "Dairy", new Measure(6, Unit.PIECES)), new Measure(6, Unit.PIECES)));
        fridgeFragment.setItems(items);

        // TODO Remove dummy ingredients
        List<Ingredient> ingredients = new ArrayList<Ingredient>();
        ingredients.add(new Ingredient("Eggs", "Dairy", new Measure(6, Unit.PIECES)));
        ingredients.add(new Ingredient("Lemons", "Fruit", new Measure(1, Unit.PIECES)));
        ingredients.add(new Ingredient("Babies", "Fats", new Measure(3, Unit.PIECES)));
        ingredients.add(new Ingredient("Peppers", "Vegetables", new Measure(2, Unit.PIECES)));
        ingredients.add(new Ingredient("Tomatoes", "Fruit", new Measure(6, Unit.PIECES)));
        ingredients.add(new Ingredient("Minced meat", "Meat", new Measure(500, Unit.GRAM)));
        ingredientsFragment.setItems(ingredients);
    }

    @Override
    public void onResult(Void v) {
        hideProgressDialog();
        // TODO WHAT TO DO HERE?
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to update/remove fridge item: " + exception.getMessage());
        hideProgressDialog();
        trackException(exception);

        // Handle regular exception
        handleException(exception);
    }

    @Override
    public void onProgress(Void... progress) {
        // do nothing
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fridge_action_add_ingredients:
                addIngredients();
                break;
        }
    }

    private void addIngredients() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(ingredientsFragment);
        ft.hide(fridgeFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onFridgeItemAdded(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        // TODO Update on backend
    }

    @Override
    public void onFridgeItemUpdated(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        // TODO Update on backend
    }

    @Override
    public void onFridgeItemRemoved(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        // TODO Remove on backend
    }

}

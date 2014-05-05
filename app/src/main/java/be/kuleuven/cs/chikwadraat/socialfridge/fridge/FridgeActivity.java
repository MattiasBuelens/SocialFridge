package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Manage fridge activity.
 */
public class FridgeActivity extends BaseActivity implements ObservableAsyncTask.Listener<Void, Void>, View.OnClickListener, IngredientsFragment.IngredientsListener {

    private static final String TAG = "FridgeActivity";

    private IngredientsFragment ingredientsFragment;
    private Button addIngredientsButton;

    private AddIngredientsTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge_my_fridge);

        ingredientsFragment = (IngredientsFragment) getSupportFragmentManager().findFragmentById(R.id.ingredients_fragment);
        addIngredientsButton = (Button) findViewById(R.id.fridge_action_add_ingredients);

        addIngredientsButton.setOnClickListener(this);

        //TODO Re-attach to close invites task ?

        // TODO Remove dummy items
        List<FridgeItem> items = new ArrayList<FridgeItem>();
        items.add(new FridgeItem("Eggs", R.drawable.eggs, new Measure(6, Unit.PIECES)));

        items.add(new FridgeItem("Lemons", R.drawable.lemons, new Measure(1, Unit.PIECES)));
        items.add(new FridgeItem("Babies", R.drawable.baby, new Measure(3, Unit.PIECES)));
        items.add(new FridgeItem("Peppers", R.drawable.peppers, new Measure(2, Unit.PIECES)));
        items.add(new FridgeItem("Tomatoes", R.drawable.tomatoes, new Measure(6, Unit.PIECES)));
        items.add(new FridgeItem("Minced meat", R.drawable.minced_meat, new Measure(500, Unit.GRAM)));
        ingredientsFragment.setIngredients(items);
    }

    @Override
    public void onResult(Void v) {
        // TODO WHAT TO DO HERE?
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to go to add ingredients: " + exception.getMessage());
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
        if (task != null) return;

        task = new AddIngredientsTask(this);
        task.execute();
        showProgressDialog(R.string.fridge_add_ingredients_progress);
    }

    @Override
    public void onFridgeItemUpdated(FridgeItem item) {
        // TODO
    }

    @Override
    public void onFridgeItemRemoved(FridgeItem item) {
        // TODO
    }

    private static class AddIngredientsTask extends ObservableAsyncTask<Void, Void, Void> {

        private AddIngredientsTask(FridgeActivity activity) {
            super(activity);
        }

        @Override
        protected Void run(Void... unused) throws Exception {
            //TODO HOWTO return new Party(parties().closeInvites(partyID, Session.getActiveSession().getAccessToken()).execute());
            return null;
        }

    }

}

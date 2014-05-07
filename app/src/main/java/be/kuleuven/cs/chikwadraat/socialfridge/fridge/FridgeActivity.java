package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.FridgeLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.IngredientsLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.fridge;

/**
 * Manage fridge activity.
 */
public class FridgeActivity extends BaseActivity implements ObservableAsyncTask.Listener<Void, FridgeItem>,
        View.OnClickListener, FridgeFragment.FridgeListener, IngredientsFragment.IngredientsListener,
        FragmentManager.OnBackStackChangedListener {

    private static final String TAG = "FridgeActivity";

    private static final int LOADER_FRIDGE = 1;
    private static final int LOADER_INGREDIENTS = 2;

    private FridgeFragment fridgeFragment;
    private IngredientsFragment ingredientsFragment;
    private Button addIngredientsButton;

    private FridgeEndpointAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge_my_fridge);

        fridgeFragment = (FridgeFragment) getSupportFragmentManager().findFragmentById(R.id.fridge_fragment);
        ingredientsFragment = (IngredientsFragment) getSupportFragmentManager().findFragmentById(R.id.ingredients_fragment);
        addIngredientsButton = (Button) findViewById(R.id.fridge_action_add_ingredients);

        addIngredientsButton.setOnClickListener(this);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // Re-attach to fridge task
        task = (FridgeEndpointAsyncTask) getLastCustomNonConfigurationInstance();
        if (task != null) {
            task.attachTransformed(this);
        }

        // Show fridge
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(fridgeFragment);
        ft.hide(ingredientsFragment);
        ft.commit();
        loadFridge();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (task != null) {
            task.detach();
        }
        return task;
    }

    private void addFridgeItem(FridgeItem item) {
        if (task != null) task.detach();

        try {
            task = new FridgeEndpointAsyncTask(this,
                    fridge().updateItem(
                            getSession().getAccessToken(),
                            item.toEndpoint()),
                    FridgeEndpointAsyncTask.Type.ADD
            );
            task.execute();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing add fridge item request: " + e.getMessage());
            trackException(e);
        }
    }

    private void updateFridgeItem(FridgeItem item) {
        if (task != null) task.detach();

        try {
            task = new FridgeEndpointAsyncTask(this,
                    fridge().updateItem(
                            getSession().getAccessToken(),
                            item.toEndpoint()),
                    FridgeEndpointAsyncTask.Type.UPDATE
            );
            task.execute();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing update fridge item request: " + e.getMessage());
            trackException(e);
        }
    }

    private void removeFridgeItem(FridgeItem item) {
        if (task != null) task.detach();

        try {
            task = new FridgeEndpointAsyncTask(this,
                    fridge().removeItem(
                            item.getIngredient().getID(),
                            getSession().getAccessToken()),
                    FridgeEndpointAsyncTask.Type.REMOVE
            );
            task.execute();
        } catch (IOException e) {
            Log.e(TAG, "Error initializing remove fridge item request: " + e.getMessage());
            trackException(e);
        }
    }

    private void removeFridgeTask() {
        if (task != null) {
            task.detach();
            task = null;
        }
    }

    @Override
    public void onResult(FridgeItem item) {
        hideProgressDialog();

        switch (task.getType()) {
            case ADD:
                afterFridgeItemAdded(item);
                break;
            case UPDATE:
                afterFridgeItemUpdated(item);
                break;
            case REMOVE:
                afterFridgeItemRemoved(item);
                break;
        }

        removeFridgeTask();
    }

    @Override
    public void onError(Exception exception) {
        Log.e(TAG, "Failed to update/remove fridge item: " + exception.getMessage());
        hideProgressDialog();
        removeFridgeTask();
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
                showIngredients();
                break;
        }
    }

    private void showIngredients() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(ingredientsFragment);
        ft.hide(fridgeFragment);
        ft.setCustomAnimations(android.R.anim.slide_in_left, 0, 0, android.R.anim.slide_out_right);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void loadFridge() {
        if (!isLoggedIn()) return;
        getSupportLoaderManager().initLoader(LOADER_FRIDGE, null, new FridgeLoaderCallbacks());
    }

    public void reloadFridge(boolean invalidateIngredients) {
        if (!isLoggedIn()) return;
        getSupportLoaderManager().restartLoader(LOADER_FRIDGE, null, new FridgeLoaderCallbacks());
        if (invalidateIngredients) {
            unloadIngredients();
        }
    }

    public void unloadFridge() {
        getSupportLoaderManager().destroyLoader(LOADER_FRIDGE);
    }

    public void loadIngredients() {
        if (!isLoggedIn()) return;
        getSupportLoaderManager().initLoader(LOADER_INGREDIENTS, null, new IngredientsLoaderCallbacks());
    }

    public void reloadIngredients(boolean invalidateFridge) {
        if (!isLoggedIn()) return;
        getSupportLoaderManager().restartLoader(LOADER_INGREDIENTS, null, new IngredientsLoaderCallbacks());
        if (invalidateFridge) {
            unloadFridge();
        }
    }

    public void unloadIngredients() {
        getSupportLoaderManager().destroyLoader(LOADER_INGREDIENTS);
    }

    @Override
    public void onBackStackChanged() {
        if (fridgeFragment.isVisible()) {
            loadFridge();
        } else if (ingredientsFragment.isVisible()) {
            loadIngredients();
        }
    }

    @Override
    public void onFridgeItemAdded(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        addFridgeItem(item);
    }

    private void afterFridgeItemAdded(FridgeItem item) {
        Log.d(TAG, "Added fridge item: " + item.getIngredient().getID());
        reloadIngredients(true);
    }

    @Override
    public void onFridgeItemUpdated(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        updateFridgeItem(item);
    }

    private void afterFridgeItemUpdated(FridgeItem item) {
        Log.d(TAG, "Updated fridge item: " + item.getIngredient().getID());
        reloadIngredients(true);
    }

    @Override
    public void onFridgeItemRemoved(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        removeFridgeItem(item);
    }

    private void afterFridgeItemRemoved(FridgeItem item) {
        Log.d(TAG, "Removed fridge item: " + item.getIngredient().getID());
        reloadFridge(true);
    }

    private class FridgeLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<FridgeItem>> {

        @Override
        public Loader<List<FridgeItem>> onCreateLoader(int id, Bundle args) {
            return new FridgeLoader(FridgeActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<FridgeItem>> loader, List<FridgeItem> items) {
            fridgeFragment.setItems(items);
        }

        @Override
        public void onLoaderReset(Loader<List<FridgeItem>> loader) {
            fridgeFragment.setItems(Collections.<FridgeItem>emptyList());
        }

    }

    private class IngredientsLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Ingredient>> {

        @Override
        public Loader<List<Ingredient>> onCreateLoader(int id, Bundle args) {
            return new IngredientsLoader(FridgeActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<List<Ingredient>> loader, List<Ingredient> items) {
            ingredientsFragment.setItems(items);
        }

        @Override
        public void onLoaderReset(Loader<List<Ingredient>> loader) {
            ingredientsFragment.setItems(Collections.<Ingredient>emptyList());
        }

    }

}

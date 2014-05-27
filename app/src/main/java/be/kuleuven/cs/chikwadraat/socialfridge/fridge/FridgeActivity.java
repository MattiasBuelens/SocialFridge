package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import com.facebook.Session;
import com.google.android.gms.analytics.HitBuilders;

import java.io.IOException;
import java.util.Collections;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeResponse;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.FridgeLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.fridge;

/**
 * Manage fridge activity.
 */
public class FridgeActivity extends BaseActivity implements ObservableAsyncTask.Listener<Void, FridgeItem>,
        View.OnClickListener, FridgeFragment.FridgeListener, IngredientsFragment.IngredientsListener {

    private static final String TAG = "FridgeActivity";

    private static final int LOADER_FRIDGE = 1;

    private FridgeFragment fridgeFragment;
    private IngredientsFragment ingredientsFragment;

    private FridgeLoaderCallbacks loaderCallbacks = new FridgeLoaderCallbacks();
    private FridgeEndpointAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fridge_my_fridge);

        fridgeFragment = (FridgeFragment) getSupportFragmentManager().findFragmentById(R.id.fridge_fragment);
        ingredientsFragment = (IngredientsFragment) getSupportFragmentManager().findFragmentById(R.id.ingredients_fragment);

        findViewById(R.id.fridge_action_add_ingredients).setOnClickListener(this);

        // Re-attach to fridge task
        task = (FridgeEndpointAsyncTask) getLastCustomNonConfigurationInstance();
        if (task != null) {
            task.attach(this);
        }

        // Show fridge
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(fridgeFragment);
        ft.hide(ingredientsFragment);
        ft.commit();
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
        Log.e(TAG, "Failed to process fridge item: " + exception.getMessage());
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
        ft.addToBackStack(null);
        ft.commit();
    }

    public void loadFridge() {
        if (!isLoggedIn()) return;
        setListsShown(false);
        getSupportLoaderManager().initLoader(LOADER_FRIDGE, null, loaderCallbacks);
    }

    public void reloadFridge() {
        if (!isLoggedIn()) return;
        setListsShown(false);
        getSupportLoaderManager().restartLoader(LOADER_FRIDGE, null, loaderCallbacks);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        loadFridge();
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        setListsShown(false);
        getSupportLoaderManager().destroyLoader(LOADER_FRIDGE);
    }

    @Override
    public void onFridgeItemAdded(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        addFridgeItem(item);
    }

    private void afterFridgeItemAdded(FridgeItem item) {
        Log.d(TAG, "Added fridge item: " + item.getIngredient().getID());

        getTracker().send(new HitBuilders.EventBuilder("Fridge", "Add")
                .setLabel(item.getIngredient().getName())
                .build());

        reloadFridge();
    }

    @Override
    public void onFridgeItemUpdated(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        updateFridgeItem(item);
    }

    private void afterFridgeItemUpdated(FridgeItem item) {
        Log.d(TAG, "Updated fridge item: " + item.getIngredient().getID());

        getTracker().send(new HitBuilders.EventBuilder("Fridge", "Update")
                .setLabel(item.getIngredient().getName())
                .build());

        reloadFridge();
    }

    @Override
    public void onFridgeItemRemoved(FridgeItem item) {
        item.setOwnerID(getLoggedInUser().getId());
        removeFridgeItem(item);
    }

    private void afterFridgeItemRemoved(FridgeItem item) {
        Log.d(TAG, "Removed fridge item: " + item.getIngredient().getID());

        getTracker().send(new HitBuilders.EventBuilder("Fridge", "Remove")
                .setLabel(item.getIngredient().getName())
                .build());

        reloadFridge();
    }

    protected void setListsShown(boolean shown) {
        fridgeFragment.setListShown(shown);
        ingredientsFragment.setListShown(shown);
    }

    private class FridgeLoaderCallbacks implements LoaderManager.LoaderCallbacks<FridgeResponse> {

        @Override
        public Loader<FridgeResponse> onCreateLoader(int id, Bundle args) {
            return new FridgeLoader(FridgeActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<FridgeResponse> loader, FridgeResponse response) {
            fridgeFragment.setItems(FridgeItem.fromEndpoint(response.getFridge()));
            ingredientsFragment.setItems(Ingredient.fromEndpoint(response.getIngredients()));
            setListsShown(true);
        }

        @Override
        public void onLoaderReset(Loader<FridgeResponse> loader) {
            fridgeFragment.setItems(Collections.<FridgeItem>emptyList());
            ingredientsFragment.setItems(Collections.<Ingredient>emptyList());
        }

    }

}

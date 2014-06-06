package be.kuleuven.cs.chikwadraat.socialfridge.dish;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;

import com.facebook.Session;

import java.util.Collections;

import be.kuleuven.cs.chikwadraat.socialfridge.BaseActivity;
import be.kuleuven.cs.chikwadraat.socialfridge.R;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeResponse;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;
import be.kuleuven.cs.chikwadraat.socialfridge.loader.FridgeLoader;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.party.CreatePartyActivity;

/**
 * Activity to view a dish.
 */
public class ViewDishActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ViewDishActivity";

    public static final String EXTRA_DISH = "dish_object";

    private static final int LOADER_FRIDGE = 1;

    private DishHeaderFragment dishHeader;
    private DishIngredientsFragment dishIngredients;
    private FridgeLoaderCallbacks loaderCallbacks = new FridgeLoaderCallbacks();

    private Dish dish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dish_view);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DISH)) {
            dish = intent.getParcelableExtra(EXTRA_DISH);
        } else {
            throw new IllegalArgumentException("Missing required dish in intent");
        }

        dishHeader = (DishHeaderFragment) getSupportFragmentManager().findFragmentById(R.id.dish_header);
        dishIngredients = (DishIngredientsFragment) getSupportFragmentManager().findFragmentById(R.id.dish_ingredients);

        findViewById(R.id.dish_action_create_party).setOnClickListener(this);

        updateDish();
    }

    protected Dish getDish() {
        return dish;
    }

    private void updateDish() {
        dishHeader.setDish(getDish());
        dishIngredients.setDish(getDish());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dish_action_create_party:
                createParty();
                break;
        }
    }

    private void createParty() {
        Intent intent = new Intent(this, CreatePartyActivity.class);
        intent.putExtra(CreatePartyActivity.EXTRA_DISH, getDish());
        startActivity(intent);
    }

    @Override
    protected void onLoggedIn(Session session, User user) {
        super.onLoggedIn(session, user);
        getSupportLoaderManager().initLoader(LOADER_FRIDGE, null, loaderCallbacks);
    }

    @Override
    protected void onLoggedOut() {
        super.onLoggedOut();
        getSupportLoaderManager().destroyLoader(LOADER_FRIDGE);
    }

    private class FridgeLoaderCallbacks implements LoaderManager.LoaderCallbacks<FridgeResponse> {

        @Override
        public Loader<FridgeResponse> onCreateLoader(int id, Bundle args) {
            return new FridgeLoader(ViewDishActivity.this);
        }

        @Override
        public void onLoadFinished(Loader<FridgeResponse> loader, FridgeResponse response) {
            dishIngredients.setFridge(FridgeItem.fromEndpoint(response.getFridge()));
        }

        @Override
        public void onLoaderReset(Loader<FridgeResponse> loader) {
            dishIngredients.setFridge(Collections.<FridgeItem>emptyList());
        }

    }

}



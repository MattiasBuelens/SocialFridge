package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.Endpoints;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.Endpoint.Dishes;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

/**
 * Retrieves dishes.
 */
public class DishesLoader extends BaseLoader<List<Dish>> {

    private static final String TAG = "DishesLoader";

    public DishesLoader(Context context) {
        super(context);
    }

    @Override
    public List<Dish> loadInBackground() {
        Dishes dishes = Endpoints.dishes(getContext());

        try {
            return Dish.fromEndpoint(dishes.getDishes().execute().getItems());
        } catch (IOException e) {
            Log.e(TAG, "Error while loading dishes: " + e.getMessage());
            trackException(e);
            return null;
        }
    }

}

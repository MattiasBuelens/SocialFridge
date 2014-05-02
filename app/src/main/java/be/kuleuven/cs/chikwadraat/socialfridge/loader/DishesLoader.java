package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.CollectionResponseDish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.dishes;

/**
 * Retrieves dishes.
 */
public class DishesLoader extends EndpointLoader<List<Dish>, CollectionResponseDish> {

    public DishesLoader(Context context) {
        super(context);
    }

    @Override
    protected EndpointRequest<CollectionResponseDish> createRequest() throws IOException {
        return dishes().getDishes();
    }

    @Override
    protected List<Dish> parseResponse(CollectionResponseDish response) {
        return Dish.fromEndpoint(response.getItems());
    }

}

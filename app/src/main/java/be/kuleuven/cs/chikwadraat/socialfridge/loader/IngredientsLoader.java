package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;

import com.facebook.Session;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.CollectionResponseIngredient;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.fridge;

/**
 * Retrieves ingredients to add to the user's fridge.
 */
public class IngredientsLoader extends EndpointLoader<List<Ingredient>, CollectionResponseIngredient> {

    public IngredientsLoader(Context context) {
        super(context);
    }

    @Override
    protected EndpointRequest<CollectionResponseIngredient> createRequest() throws IOException {
        return fridge().getIngredients(Session.getActiveSession().getAccessToken());
    }

    @Override
    protected List<Ingredient> parseResponse(CollectionResponseIngredient response) {
        return Ingredient.fromEndpoint(response.getItems());
    }

}

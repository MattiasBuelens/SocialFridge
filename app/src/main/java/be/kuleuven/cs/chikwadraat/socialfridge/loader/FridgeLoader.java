package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;

import com.facebook.Session;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.CollectionResponseFridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.fridge;

/**
 * Retrieves ingredients to add to the user's fridge.
 */
public class FridgeLoader extends EndpointLoader<List<FridgeItem>, CollectionResponseFridgeItem> {

    public FridgeLoader(Context context) {
        super(context);
    }

    @Override
    protected EndpointRequest<CollectionResponseFridgeItem> createRequest() throws IOException {
        return fridge().getFridge(Session.getActiveSession().getAccessToken());
    }

    @Override
    protected List<FridgeItem> parseResponse(CollectionResponseFridgeItem response) {
        return FridgeItem.fromEndpoint(response.getItems());
    }

}

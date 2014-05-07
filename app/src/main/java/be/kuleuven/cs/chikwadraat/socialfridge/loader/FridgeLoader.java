package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;

import com.facebook.Session;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeResponse;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.fridge;

/**
 * Retrieves ingredients to add to the user's fridge.
 */
public class FridgeLoader extends EndpointLoader<FridgeResponse, FridgeResponse> {

    public FridgeLoader(Context context) {
        super(context);
    }

    @Override
    protected EndpointRequest<FridgeResponse> createRequest() throws IOException {
        return fridge().getFridge(Session.getActiveSession().getAccessToken());
    }

    @Override
    protected FridgeResponse parseResponse(FridgeResponse response) {
        return response;
    }

}

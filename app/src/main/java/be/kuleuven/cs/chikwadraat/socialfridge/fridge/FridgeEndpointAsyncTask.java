package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.util.EndpointAsyncTask;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created by Mattias on 7/05/2014.
 */
public class FridgeEndpointAsyncTask extends EndpointAsyncTask<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem, FridgeItem> {

    private final Type type;

    public FridgeEndpointAsyncTask(Listener<Void, FridgeItem> listener, EndpointRequest<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem> request, Type type) {
        super(listener, request);
        this.type = checkNotNull(type);
    }

    @Override
    protected FridgeItem parseResponse(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem response) {
        return new FridgeItem(response);
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ADD,
        UPDATE,
        REMOVE
    }

}

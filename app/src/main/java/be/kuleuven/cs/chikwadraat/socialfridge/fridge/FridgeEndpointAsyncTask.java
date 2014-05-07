package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.util.EndpointAsyncTask;

/**
* Created by Mattias on 7/05/2014.
*/
class FridgeEndpointAsyncTask extends EndpointAsyncTask<FridgeItem> {

    private final boolean isAdd;

    public FridgeEndpointAsyncTask(Listener<Void, FridgeItem> listener, EndpointRequest<FridgeItem> request, boolean isAdd) {
        super(listener, request);
        this.isAdd = isAdd;
    }

    public boolean isAdd() {
        return isAdd;
    }

}

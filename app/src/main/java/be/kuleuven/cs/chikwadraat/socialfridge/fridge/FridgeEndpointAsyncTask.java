package be.kuleuven.cs.chikwadraat.socialfridge.fridge;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.model.FridgeItem;
import be.kuleuven.cs.chikwadraat.socialfridge.util.EndpointAsyncTask;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;
import be.kuleuven.cs.chikwadraat.socialfridge.util.TransformedObservableAsyncTaskListener;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created by Mattias on 7/05/2014.
 */
public class FridgeEndpointAsyncTask extends EndpointAsyncTask<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem> {

    private final Type type;

    public FridgeEndpointAsyncTask(Listener<Void, FridgeItem> listener, EndpointRequest<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem> request, Type type) {
        super(new FridgeListener(listener), request);
        this.type = checkNotNull(type);
    }

    public void attachTransformed(Listener<Void, FridgeItem> listener) {
        super.attach(new FridgeListener(listener));
    }

    @Deprecated
    @Override
    public void attach(Listener<Void, be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem> listener) {
        super.attach(listener);
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ADD,
        UPDATE,
        REMOVE
    }

    private static class FridgeListener extends TransformedObservableAsyncTaskListener<Void, be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem, FridgeItem> {

        protected FridgeListener(ObservableAsyncTask.Listener<Void, FridgeItem> listener) {
            super(listener);
        }

        @Override
        protected FridgeItem transformResult(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.FridgeItem result) {
            return new FridgeItem(result);
        }

    }

}

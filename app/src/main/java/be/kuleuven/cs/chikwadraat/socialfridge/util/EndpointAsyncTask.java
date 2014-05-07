package be.kuleuven.cs.chikwadraat.socialfridge.util;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;

/**
 * Created by Mattias on 2/05/2014.
 */
public abstract class EndpointAsyncTask<R, T> extends ObservableAsyncTask<Void, Void, T> {

    protected final EndpointRequest<R> request;

    protected EndpointAsyncTask(Listener<Void, T> listener, EndpointRequest<R> request) {
        super(listener);
        this.request = request;
    }

    @Override
    protected T run(Void... params) throws Exception {
        return parseResponse(request.execute());
    }

    protected abstract T parseResponse(R response);

}

package be.kuleuven.cs.chikwadraat.socialfridge.util;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;

/**
 * Created by Mattias on 2/05/2014.
 */
public class EndpointAsyncTask<T> extends ObservableAsyncTask<Void, Void, T> {

    protected final EndpointRequest<T> request;

    public EndpointAsyncTask(Listener<Void, T> listener, EndpointRequest<T> request) {
        super(listener);
        this.request = request;
    }

    @Override
    protected T run(Void... params) throws Exception {
        return request.execute();
    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;

/**
 * Loader from an endpoint.
 */
public abstract class EndpointLoader<T, R> extends BaseLoader<T> {

    private static final String TAG = "EndpointLoader";

    public EndpointLoader(Context context) {
        super(context);
    }

    protected abstract EndpointRequest<R> createRequest() throws IOException;

    protected abstract T parseResponse(R response);

    @Override
    public T loadInBackground() {
        try {
            return parseResponse(createRequest().execute());
        } catch (IOException e) {
            Log.e(TAG, "Error while loading from endpoint: " + e.getMessage());
            trackException(e);
            return null;
        }
    }
}

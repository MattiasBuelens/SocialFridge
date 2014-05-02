package be.kuleuven.cs.chikwadraat.socialfridge.party;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;

/**
 * Created by Mattias on 2/05/2014.
 */
public class PartyEndpointAsyncTask extends ObservableAsyncTask<Void, Void, Party> {

    private final EndpointRequest<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> request;

    public PartyEndpointAsyncTask(Listener<Void, Party> listener, EndpointRequest<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> request) {
        super(listener);
        this.request = request;
    }

    @Override
    protected Party run(Void... params) throws Exception {
        be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party result = request.execute();
        if (result == null) return null;
        return new Party(result);
    }

}

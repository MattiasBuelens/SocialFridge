package be.kuleuven.cs.chikwadraat.socialfridge.party;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.util.EndpointAsyncTask;

/**
 * Created by Mattias on 2/05/2014.
 */
public class PartyEndpointAsyncTask extends EndpointAsyncTask<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party, Party> {

    public PartyEndpointAsyncTask(Listener<Void, Party> listener, EndpointRequest<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> request) {
        super(listener, request);
    }

    @Override
    protected Party parseResponse(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party response) {
        return new Party(response);
    }

}

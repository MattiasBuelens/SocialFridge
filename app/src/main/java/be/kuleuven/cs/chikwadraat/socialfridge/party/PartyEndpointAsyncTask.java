package be.kuleuven.cs.chikwadraat.socialfridge.party;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;
import be.kuleuven.cs.chikwadraat.socialfridge.util.EndpointAsyncTask;
import be.kuleuven.cs.chikwadraat.socialfridge.util.ObservableAsyncTask;
import be.kuleuven.cs.chikwadraat.socialfridge.util.TransformedObservableAsyncTaskListener;

/**
 * Created by Mattias on 2/05/2014.
 */
public class PartyEndpointAsyncTask extends EndpointAsyncTask<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> {

    public PartyEndpointAsyncTask(Listener<Void, Party> listener, EndpointRequest<be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> request) {
        super(new PartyListener(listener), request);
    }

    public void attachTransformed(Listener<Void, Party> listener) {
        super.attach(new PartyListener(listener));
    }

    @Deprecated
    @Override
    public void attach(Listener<Void, be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party> listener) {
        super.attach(listener);
    }

    private static class PartyListener extends TransformedObservableAsyncTaskListener<Void, be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party, Party> {

        protected PartyListener(ObservableAsyncTask.Listener<Void, Party> listener) {
            super(listener);
        }

        @Override
        protected Party transformResult(be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.Party result) {
            return new Party(result);
        }

    }

}

package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;

import com.facebook.Session;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.CollectionResponseParty;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Party;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.users;

/**
 * Retrieves a user's parties.
 */
public class PartiesLoader extends EndpointLoader<List<Party>, CollectionResponseParty> {

    public PartiesLoader(Context context) {
        super(context);
    }

    @Override
    protected EndpointRequest<CollectionResponseParty> createRequest() throws IOException {
        return users().getParties(Session.getActiveSession().getAccessToken());
    }

    @Override
    protected List<Party> parseResponse(CollectionResponseParty response) {
        return Party.fromEndpoint(response.getItems());
    }

}

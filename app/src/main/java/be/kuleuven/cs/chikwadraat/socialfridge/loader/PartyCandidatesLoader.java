package be.kuleuven.cs.chikwadraat.socialfridge.loader;

import android.content.Context;

import com.facebook.Session;

import java.io.IOException;
import java.util.List;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.EndpointRequest;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.CollectionResponsePartyMember;
import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.PartyMember;

import static be.kuleuven.cs.chikwadraat.socialfridge.Endpoints.parties;

/**
 * Retrieves candidates for a party.
 */
public class PartyCandidatesLoader extends EndpointLoader<List<PartyMember>, CollectionResponsePartyMember> {

    private final long partyID;

    public PartyCandidatesLoader(Context context, long partyID) {
        super(context);
        this.partyID = partyID;
    }

    public long getPartyID() {
        return partyID;
    }

    @Override
    protected EndpointRequest<CollectionResponsePartyMember> createRequest() throws IOException {
        return parties().getCandidates(partyID, Session.getActiveSession().getAccessToken());
    }

    @Override
    protected List<PartyMember> parseResponse(CollectionResponsePartyMember response) {
        return response.getItems();
    }

}

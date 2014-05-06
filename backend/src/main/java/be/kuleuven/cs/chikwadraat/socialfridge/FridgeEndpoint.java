package be.kuleuven.cs.chikwadraat.socialfridge;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiNamespace;

@Api(
        name = "endpoint",
        version = "v2",
        namespace = @ApiNamespace(ownerDomain = "chikwadraat.cs.kuleuven.be", ownerName = "Chi Kwadraat", packagePath = "socialfridge")
)
public class FridgeEndpoint {

    
}

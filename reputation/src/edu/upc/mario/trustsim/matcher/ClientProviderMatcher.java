package edu.upc.mario.trustsim.matcher;

import edu.upc.mario.trustsim.Client;
import edu.upc.mario.trustsim.Cluster;
import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Task;

import java.util.List;

/**
 *
 * @author Mario
 */
public interface ClientProviderMatcher {
    public Provider matchClientProviders(Task t, List<Provider> providers);
}

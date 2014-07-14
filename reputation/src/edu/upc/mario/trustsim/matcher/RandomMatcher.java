package edu.upc.mario.trustsim.matcher;

import edu.upc.mario.trustsim.Client;
import edu.upc.mario.trustsim.Cluster;
import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Task;

import java.util.List;
import java.util.Random;

/**
 *
 * @author Mario
 */
public class RandomMatcher implements ClientProviderMatcher {
    private static Random rnd = new Random(System.currentTimeMillis());
    
    @Override
    public Provider matchClientProviders(Task t, List<Provider> providers) {
        
        // For the previous tests, instead of a true competition in which providers compete for the clients
        // by offering prices in function to their true reputation, the provider is chosen randomly
        // and then get the price in function of the reputation (some kind of inverse negotiation)
        // this is done by two reasons:
        // 1 - Avoid in the simulation the study of private policies from clients
        // 2 - Distribute the probability of selling resources, so the reputation model can be evaluated more
        // accurately, without punctual market influences derived from the simulation (not a real market)

        Client ci = t.getClient();
        Cluster cu = ci.getBelongingCluster();
        Provider p = providers.get(rnd.nextInt(providers.size()));


        double price =  t.getSlo() * cu.getReputation(p.getIdentifier());


        return p;
        
        
//        Provider matched = null;
//        double minPrice = Double.MAX_VALUE;
//        Vector sloImportances = ci.getSloImportances();
//        for(Provider p : providers) {    
//            
//            Vector rawReputation = cu.getReputation(p.getIdentifier());
//            
//            Vector sloImportances = ci.getSloImportances();
//            
//            // todo: add this description to the paper
//            Vector reputation = rawReputation.multiplyTerms(sloImportances);
//
//                        
//            Vector slos = ci.getSLO();
//            
//            Vector multiplied = slos.multiplyTerms(reputation);
//            
//            double price = multiplied.summarize();            
//            
//            if(price < minPrice) {
//                minPrice = price;
//                matched = p;
//            }            
//        }
        
//        return matched;
    }

    
}

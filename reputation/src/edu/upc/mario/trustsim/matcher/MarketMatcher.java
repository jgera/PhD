package edu.upc.mario.trustsim.matcher;

import edu.upc.mario.trustsim.Client;
import edu.upc.mario.trustsim.Cluster;
import edu.upc.mario.trustsim.Const;
import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author Mario
 */
public class MarketMatcher implements ClientProviderMatcher {

    // Matching clients and providers according to the reputation model of section 3
    @Override
    public Provider matchClientProviders(Task task, List<Provider> providers) {
        Client ci = task.getClient();
        Cluster cu = ci.getBelongingCluster();
        
        Provider matched = null;
        double matchedScore = Double.NEGATIVE_INFINITY;

        // test providers randomly
        List<Provider> randomProviders = new LinkedList<Provider>();
        for(Provider p : providers) {
            randomProviders.add(rnd.nextInt(randomProviders.size()+1), p);
        }
                
//        System.out.print(ci.getIdentifier() + " checks ");
        
        for(Provider p : randomProviders) {
            Double providerTrust = null;
            Double dt = ci.getProviderDirectTrust(p.getIdentifier());
            Double rt = cu.getReputation(p.getIdentifier());
            if(dt == null && rt == null) {
                providerTrust = 0.5;
            } else if(dt == null) {
                providerTrust = rt;                
            } else if(rt == null) {
                providerTrust = dt;
            } else {
                final double w1 = 0.5, w2 = 0.5;
                
                Double t1 = dt * w1;
                Double t2 = rt * w2;
                
                providerTrust = t1 + t2;
            }
            
//            double price = ci.getSLO().summarize();
            double price = p.getPrice(task);

            double score = -providerTrust / price;
            //double score = -sloImportances.divideTerms(providerTrust).summarize();
            
//            System.out.print(p.getIdentifier() + "("+score+")");
            if(p.isTaskFitting(task))
            if(score > matchedScore || matched == null) {
                matchedScore = score;
                matched = p;
                task.setRevenueFunction(price);
//                System.out.print("*");
            }
//            System.out.print(" ");
                        
        }
//        System.out.println("");
        
        return matched;
    }
    
    private static final Random rnd = new Random(System.currentTimeMillis());
    
}

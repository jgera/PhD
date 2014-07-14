package edu.upc.mario.trustsim;

import edu.upc.mario.trustsim.wl.Task;

import java.util.*;


public class Client {
    
    protected String identifier;

    private Cluster belongingCluster;
    private double reputation;
    
    // vector of direct reputation through several cloud providers. It is not normalized with sloPonderationVector
    // TODO: decrease reputation progressively
    protected Map<String,Double> providerRawDirectTrusts = new HashMap<String, Double>();
    private static final Random rnd = new Random(System.currentTimeMillis());

    public Client(String identifier, Cluster belongingCluster) {
        this.identifier = identifier;
        this.belongingCluster = belongingCluster;
        while(reputation <= 0) {
            reputation =  1 - Math.abs(rnd.nextGaussian() / 5);
        }
    }

    public double getReputation() {
        return reputation;
    }

    public void updateTrust(Task task, String providerId) {
        Double providerTrust = providerRawDirectTrusts.get(providerId);
        if(providerTrust == null) {
            providerTrust = task.getSLAFulfillmentRate();
        } else {
            providerTrust = task.getSLAFulfillmentRate() * Const.TRUST_RECOVERY_RATE + (1 - Const.TRUST_RECOVERY_RATE) * providerTrust;
        }
        providerRawDirectTrusts.put(providerId, providerTrust);
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public Set<String> getKnownProviderIds() {
        return providerRawDirectTrusts.keySet();
    }
    
    public Double getProviderDirectTrust(String providerId) {
        return providerRawDirectTrusts.get(providerId);
    }
    
//    /**
//     * Simulates the provider usage by client. After that, it stores the direct
//     * trust derived by the received QoS.
//     * @param provider
//     */
//    public void useProvider(Provider provider) {
//        double qos = provider.getQoS();
//        TrustClientToProvider trust = new TrustClientToProvider(this, provider, qos);
//        providerRawDirectTrusts.put(provider.getIdentifier(), trust);
//    }

    public Cluster getBelongingCluster() {
        return belongingCluster;
    }


    
}
package edu.upc.mario.trustsim;

import java.util.*;


public class Cluster implements Iterable<Client> {
            
    protected Map<String,Double> providerReputations = new HashMap<String, Double>();
    protected Map<String,Client> clients = new TreeMap<String, Client>();
    private static final Random RND = new Random(System.currentTimeMillis());

    private class Report {
        double directTrust;
        Client client;
        private Report(Client client, double directTrust) {
            this.client = client;

            // the direct trust will be biased in function of the client reputation
            this.directTrust = directTrust * client.getReputation(); // + directTrust * RND.nextGaussian() * (1 - client.getReputation());
            if(directTrust > 1) {
                this.directTrust = 1;
            } else if(directTrust < 0) {
                this.directTrust = 0;
            }
        }
    }
    /**
     * Recalculates the trust with all the clients and providers
     * 
     */
    public void recalculateAllTrust() {
        // key : provider id
        // value : a list of all reports
        Map<String,List<Report>> allReports = new HashMap<String, List<Report>>();

        // all the clients report feedback for all the providers they know
        // todo: add a time window to discard old direct trusts
        for(Client theClient : this) {
            for(String providerId : theClient.getKnownProviderIds()) {
                Double providerTrust = theClient.getProviderDirectTrust(providerId);
                List<Report> allReportsForProvider = allReports.get(providerId);
                if(allReportsForProvider == null) {
                    allReportsForProvider = new ArrayList<Report>();
                    allReports.put(providerId, allReportsForProvider);
                }
                allReportsForProvider.add(new Report(theClient, providerTrust));
            }
        }


        // for each of the reported providers, its trust is calculated in base of the previous reports
        for(Map.Entry<String,List<Report>> trustEntry : allReports.entrySet()) {
            double providerTrust = 0;

            String providerId = trustEntry.getKey();
            double clientReputationsSum = 0;

            for(Report clientReport : trustEntry.getValue()) {
                providerTrust +=  clientReport.directTrust * clientReport.client.getReputation();
                clientReputationsSum +=  clientReport.client.getReputation();
            }

            providerTrust /= clientReputationsSum;

            providerReputations.put(providerId, providerTrust);
            
            //System.out.println("Cluster trust to " + provider.getIdentifier() + ": " + providerTrust);
        }
    }
    
    public void addClient(Client c) {
        clients.put(c.getIdentifier(), c);
        
    }
    

    public double getReputation(String providerId) {
        Double p = providerReputations.get(providerId);
        return p == null ? Const.UNKNOWN_REPUTATION : p;
    }    

    @Override
    public Iterator<Client> iterator() {
        return clients.values().iterator();
    }

}

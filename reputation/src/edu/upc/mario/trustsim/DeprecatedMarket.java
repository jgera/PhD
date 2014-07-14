package edu.upc.mario.trustsim;


public class DeprecatedMarket {
/*
    private static long time = 0;
    
    public static long getTime() {
        return time;
    }
    
    private static final long SIMULATION_DURATION = 100;

    public static void doSimulation1() {
        ClientProviderMatcher matcher = new RandomMatcher();
        time = 0;
        
        List<Provider> providers = new ArrayList<Provider>();
        Provider pr = null;
        for(int i = 0 ; i < 3 ; i++) {
            pr = new Provider("p"+i);
            providers.add(pr);            
        }
        Provider broken1 = new BrokenProvider("p4(broken)", SIMULATION_DURATION / 3, 2*SIMULATION_DURATION / 3, new Vector(new double[] {0.5, 1,1}));        
        providers.add(broken1);
        
        Provider cheater = new BrokenProvider("p5(dishonest)", 0, new Vector(new double[] { 0.6, 0.6, 0.6 }));
        providers.add(cheater);
        
        Cluster cluster = new Cluster();
        
        cluster.addClient(new Client("ws_0", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);
        cluster.addClient(new Client("ws_1", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);
        cluster.addClient(new Client("ws_2", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);
        cluster.addClient(new Client("ws_3", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);
        cluster.addClient(new Client("ws_4", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);
        cluster.addClient(new Client("ws_5", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);
        cluster.addClient(new Client("ws_6", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);
        cluster.addClient(new Client("ws_7", new Vector(new double[]{4,4,8}), new Vector(new double[]{0.4,0.4,1})),0.5);

        cluster.addClient(new Client("db_0", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_1", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_2", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_3", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_4", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_5", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_6", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new DishonestClient("db_7(cheat)", new Vector(new double[]{2,8,6}), new Vector(new double[]{0.2,0.8,0.6}),pr,SIMULATION_DURATION/2),0.5);    
        
        cluster.addClient(new Client("batch_0", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1})),0.5);
        cluster.addClient(new Client("batch_1", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1})),0.5);
        cluster.addClient(new Client("batch_2", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1})),0.5);
        cluster.addClient(new Client("batch_3", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1})),0.5);
        cluster.addClient(new Client("batch_4", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1})),0.5);
        cluster.addClient(new Client("batch_5", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1})),0.5);
        cluster.addClient(new Client("batch_6", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1})),0.5);
        cluster.addClient(new DishonestClient("batch_7(cheat)", new Vector(new double[]{8,4,1}), new Vector(new double[]{1,0.5,0.1}),cheater),0.5);
                
        PrintWriter clientsResults = null, providersResults = null;
        try {
            clientsResults = new PrintWriter(new FileOutputStream("clients.csv"));
            clientsResults.println(cluster.getClientNames());
            
            providersResults = new PrintWriter(new FileOutputStream("providers.csv"));
            for(Provider p : providers) {
                for(int i = 0 ; i < Trust.SLOs_VECTOR_LENGTH ; i++) {
                    providersResults.print(p.getIdentifier() + "("+i+") ");
                }
            }
            providersResults.println();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TrustSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i = 0 ; i < SIMULATION_DURATION ; i++) {
            clientsResults.println(cluster.getTrusts());
            
            time = i;
            
            for (TrustClusterToClient c : cluster.clients.values()) {
                Provider p = matcher.matchClientProviders(c.getClient(), cluster, providers);
                c.getClient().useProvider(p);
            }            
            cluster.recalculateAllTrust();            
            
            for(Provider p : providers) {
                Vector reputation = cluster.getReputation(p.getIdentifier());
                for(int d = 0 ; d < Trust.SLOs_VECTOR_LENGTH ; d++) {
                    providersResults.print(reputation.valueAt(d) + " ");                    
                }
            }
            providersResults.println();            
        }
        
        clientsResults.println(cluster.getTrusts());

            
        clientsResults.close();  
        providersResults.close();
    }
    
    public static void doSimulation2() {
        ClientProviderMatcher matcher = new MarketMatcher();
        time = 0; 
        
        
        List<Provider> providers = new ArrayList<Provider>();
        providers.add(new Provider("p1"));
//        providers.add(new Provider("p2"));
        providers.add(new BrokenProvider("p2(1dbroken)", 0, new Vector(new double[] {.3,1,1})));
        providers.add(new BrokenProvider("p3(2dbroken)", 0, new Vector(new double[] {1,.3,1})));
        providers.add(new BrokenProvider("p4(3dbroken)", 0, new Vector(new double[] {1,1,.3})));
        
        Cluster cluster = new Cluster();

        cluster.addClient(new Client("comp_0", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        cluster.addClient(new Client("comp_1", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        cluster.addClient(new Client("comp_2", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        cluster.addClient(new Client("comp_3", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        cluster.addClient(new Client("comp_4", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        cluster.addClient(new Client("comp_5", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        cluster.addClient(new Client("comp_6", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        cluster.addClient(new Client("comp_7", new Vector(new double[]{7,7,7}), new Vector(new double[]{1,1,1})),0.5);
        
        cluster.addClient(new Client("batch_0", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);
        cluster.addClient(new Client("batch_1", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);
        cluster.addClient(new Client("batch_2", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);
        cluster.addClient(new Client("batch_3", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);
        cluster.addClient(new Client("batch_4", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);
        cluster.addClient(new Client("batch_5", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);
        cluster.addClient(new Client("batch_6", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);
        cluster.addClient(new Client("batch_7", new Vector(new double[]{7,2,2}), new Vector(new double[]{1,0.3,0.3})),0.5);

        cluster.addClient(new Client("db_0", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_1", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_2", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_3", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_4", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_5", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_6", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        cluster.addClient(new Client("db_7", new Vector(new double[]{1,8,4}), new Vector(new double[]{0.2,0.8,0.6})),0.5);
        
        cluster.addClient(new Client("ws_0", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);
        cluster.addClient(new Client("ws_1", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);
        cluster.addClient(new Client("ws_2", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);
        cluster.addClient(new Client("ws_3", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);
        cluster.addClient(new Client("ws_4", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);
        cluster.addClient(new Client("ws_5", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);
        cluster.addClient(new Client("ws_6", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);
        cluster.addClient(new Client("ws_7", new Vector(new double[]{4,4,8}), new Vector(new double[]{.6,0.3,1})),0.5);

        
        
        Map<String,Map<String,Integer>> chosenProviders = new TreeMap<String,Map<String,Integer>>();
        
        // Create a table to initialize which client choose which provider, and how many times. Initializes to 0
        for (TrustClusterToClient c : cluster.clients.values()) {
            String clientId = c.getClient().getIdentifier();
            Map<String,Integer> numberOfChoices = new TreeMap<String,Integer>();
            chosenProviders.put(clientId, numberOfChoices);
            for(Provider p : providers) {
                numberOfChoices.put(p.getIdentifier(), 0);
            }
        }
                
        for(int i = 0 ; i < SIMULATION_DURATION ; i++) {
            
            time = i;
            
            
            for (TrustClusterToClient c : cluster.clients.values()) {
                Provider p = matcher.matchClientProviders(c.getClient(), cluster, providers);
                c.getClient().useProvider(p);
                
                Map<String,Integer> usages = chosenProviders.get(c.getClient().getIdentifier());
                if(usages == null) {
                    usages = new TreeMap<String, Integer>();
                    chosenProviders.put(c.getClient().getIdentifier(), usages);                    
                }
                
                Integer numberUsages = usages.get(p.getIdentifier());
                if(numberUsages == null) {
                    numberUsages = 0;
                }
                numberUsages++;
                usages.put(p.getIdentifier(), numberUsages);
                
            }            
            cluster.recalculateAllTrust();                        
        }        
        
        PrintStream out = System.out;
                       
        //FOR DEBUGGING all the columns correspond to the same provider
        String providerIds = null;
        for(Map<String, Integer> m : chosenProviders.values()) {
            String s = "- ";
            for(String k : m.keySet()) {
                s += k +" ";
            }
            if(providerIds != null && !s.equals(providerIds)) {
                throw new RuntimeException("Not all the providers are located in the same row, or any provider is missing");
            }
            providerIds = s;
        }        
        
        // PRINT THE NUMBER OF TIMES A CLIENT USED A PROVIDER  
        out.println(providerIds);
        for(String clientId : chosenProviders.keySet()) {
            Map<String, Integer> m = chosenProviders.get(clientId);
            String s = clientId + " ";
            for(String k : m.keySet()) {
                s += m.get(k) + " ";
            }
            out.println(s);            
        }     
    }
    
*/
}

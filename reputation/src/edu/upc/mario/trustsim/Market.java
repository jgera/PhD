package edu.upc.mario.trustsim;

import edu.upc.mario.trustsim.matcher.MarketMatcher;
import edu.upc.mario.trustsim.tasks.AgnosticTaskCanceler;
import edu.upc.mario.trustsim.tasks.ContextAwareCanceler;
import edu.upc.mario.trustsim.tasks.ReputationTaskCanceler;
import edu.upc.mario.trustsim.tasks.RevenueTaskCanceler;
import edu.upc.mario.trustsim.util.Output;
import edu.upc.mario.trustsim.wl.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Market {
    private static final Random rnd = new Random(System.currentTimeMillis());
    private List<Provider> providers = new ArrayList<Provider>();
    private Cluster cluster = new Cluster();


    int cnS = 0;
    public void simulate() {
        Provider p0 = new Provider("normal");
        p0.setTaskCanceler(new AgnosticTaskCanceler(p0));
        Provider p1 = new Provider("revenueAware");
        p1.setTaskCanceler(new RevenueTaskCanceler(p1));
        Provider p2 = new Provider("reputationAware");
        p2.setTaskCanceler(new ReputationTaskCanceler(p2));
//        Provider p3 = new Provider("contextAware");
//        p3.setTaskCanceler(new ContextAwareCanceler(p3));

        providers.add(p0);
        providers.add(p1);
        providers.add(p2);
//        providers.add(p3);

        for(int i = 0 ; i < Const.NUMBER_OF_CLIENTS ; i++) {
            cluster.addClient(new Client("c"+i, cluster));
        }

        //Client c = new Client("fake", new Vector())
        MarketMatcher mm = new MarketMatcher();
        for(int step = 0 ; step < Const.SIMULATION_STEPS ; step++) {
            if(step < Const.SIMULATION_STEPS - Const.TASK_MAX_DURATION) {
                boolean first = true;
                int cn = 0;
                // Random-order list in each step
                ArrayList<Client> unorderedClients = new ArrayList<Client>();
                for(Client c : cluster) {
                    unorderedClients.add(rnd.nextInt(unorderedClients.size()+1),c);
                }
                for(Client c : unorderedClients) {
                    double frequency = Math.round(
                            (Const.MIN_DEPLOYMENT_FREQUENCY + Const.MAX_DEPLOYMENT_FREQUENCY) / 2
                            + Math.sin( (double)((cnS+cn+step)%Const.STEPS_PER_CYCLE)*(2*Math.PI)/(double)Const.STEPS_PER_CYCLE) *
                                    (Const.MIN_DEPLOYMENT_FREQUENCY - Const.MAX_DEPLOYMENT_FREQUENCY)/2);

                    if(first) Output.REPUTATIONS.println(""+frequency);

                    first = false;

                    if((cn+step) % (int)frequency == 0) {
                        Task t = new Task(c, Math.ceil(rnd.nextDouble() * Const.TASK_MAX_CPUs) ,step,
                                rnd.nextInt(Const.TASK_MAX_DURATION - Const.TASK_MIN_DURATION) + Const.TASK_MIN_DURATION);
                        assert t.getSlo() >= 0.99;
                        assert t.getSlo() <= 4.01;
                        Provider p = mm.matchClientProviders(t, providers);
                            if(p != null) {
                                boolean allocated = p.allocateTask(t);
                                assert allocated == true;
                            }
                    }
                    cn++;
                }

            }
            cnS++;

            for(Provider p : providers) {
                p.step(step);
            }

            cluster.recalculateAllTrust();

            StringBuilder provInfo = new StringBuilder(""+step);
            StringBuilder revenues = new StringBuilder(""+step);
            for(Provider p : providers) {
                provInfo.append(" " + p.getStepPenalty() + " "
                        + cluster.getReputation(p.getIdentifier()) + " "
                        + p.getStepFulfillmentRate()
                        + " " + p.getWorkloadPercentage()
                );
                revenues.append(" " + p.getRevenueIncrementAtWindow(step));
            }


            Output.PROVIDERS.println(provInfo.toString());
            Output.EARNINGS.println(revenues.toString());
            //Output.REPUTATIONS.println( + " " + cluster.getReputation(p2.getIdentifier()));
        }
        for(Provider p : providers) {
            System.out.println(p.getIdentifier() + ": " + p.getRevenue());

        }
    }
}

package edu.upc.mario.trustsim.tasks;

import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Node;
import edu.upc.mario.trustsim.wl.Task;

import java.util.Set;

public class ContextAwareCanceler extends TaskCanceler {

    ReputationTaskCanceler reputationTaskCanceler;
    RevenueTaskCanceler revenueTaskCanceler;

    public ContextAwareCanceler(Provider provider) {
        super(provider);
        reputationTaskCanceler = new ReputationTaskCanceler(provider);
        revenueTaskCanceler = new RevenueTaskCanceler(provider);
    }

    @Override
    public Set<Task> selectSLAsToViolate(Node n, double load, long time) {
        TaskCanceler currentCanceler;
        if(provider.isFailure() //|| provider.getWorkloadPercentage() > 0.9
                ) {
            currentCanceler = reputationTaskCanceler;
        } else {
            currentCanceler = revenueTaskCanceler;
        }
        return currentCanceler.selectSLAsToViolate(n,load,time);
    }
}

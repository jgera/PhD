package edu.upc.mario.trustsim.tasks;

import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Node;
import edu.upc.mario.trustsim.wl.Task;

import java.util.*;

public class ReputationTaskCanceler extends TaskCanceler {
    public ReputationTaskCanceler(Provider provider) {
        super(provider);
    }

    @Override
    public Set<Task> selectSLAsToViolate(Node n, double load, final long time) {
        List<Task> list = new ArrayList<Task>();
        for(Task t : n.getAllocatedTasks()) {
            list.add(t);
        }
        Collections.sort(list, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o2.getClient().getReputation() < o1.getClient().getReputation() ? 1 : -1;

                //other mode:
                // check if actually there is a violation, because vt > MRT
//                double f1 = Math.abs(t1.getRevenueIfCancelledAtTime(time) - t1.getMaxRevenue()) < 0.01 ? 1 : 0;
//                double f2 = Math.abs(t2.getRevenueIfCancelledAtTime(time) - t2.getMaxRevenue()) < 0.01 ? 1 : 0;
//
//                return (t2.getClient().getReputation() + f2) < (t1.getClient().getReputation() + f1) ? 1 : -1;
            }
        });
        Set<Task> tasksToViolate = new HashSet<Task>();

        double cpus = Math.ceil(load * n.getCpus());
        int i = 0;
        while(i < list.size() && cpus > n.getCpus()) {
            tasksToViolate.add(list.get(i));
            cpus -= n.getCpus();
            i++;
        }
        if(tasksToViolate.size() > 0) {
            System.out.print(provider.getIdentifier()+ ". " +"Reputation Aware. Canceling tasks: ");
            for(Task t : tasksToViolate) {
                System.out.print(" " + t.getClient().getReputation());
            }
            System.out.println();
        }
        return tasksToViolate;
    }

}

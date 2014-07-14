package edu.upc.mario.trustsim.tasks;

import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Node;
import edu.upc.mario.trustsim.wl.Task;

import java.util.*;

public class RevenueTaskCanceler extends TaskCanceler {

    public RevenueTaskCanceler(Provider provider) {
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
            public int compare(Task t1, Task t2) {
                return t1.getRevenueIfCancelledAtTime(time) <= t2.getRevenueIfCancelledAtTime(time) ? 1 : -1;
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
            System.out.print(provider.getIdentifier()+ ". " +"Revenue Aware. Canceling tasks: ");
            for(Task t : tasksToViolate) {
                System.out.print(" " + t.getRevenueIfCancelledAtTime(time));
            }
            System.out.println();
        }
        return tasksToViolate;
    }

}

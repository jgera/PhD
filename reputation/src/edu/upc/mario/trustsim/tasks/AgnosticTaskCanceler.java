package edu.upc.mario.trustsim.tasks;

import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Node;
import edu.upc.mario.trustsim.wl.Task;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class AgnosticTaskCanceler extends TaskCanceler {

    private static final Random RND = new Random(System.currentTimeMillis());

    public AgnosticTaskCanceler(Provider provider) {
        super(provider);
    }

    public Set<Task> selectSLAsToViolate(Node n, double load, long time) {
        // this provider does random cancelation of tasks until the load rate comes under 1
        double cpus = Math.ceil(n.getCpus() * load);
        Set<Task> tasksToCancel = new HashSet<Task>();
        Task[] allocatedTasks = n.getAllocatedTasks();
        while(cpus > n.getCpus()) {
            Task cancel = null;
            int cancelIndex = RND.nextInt(allocatedTasks.length);
            int startIndex = cancelIndex;
            do {
                cancel = allocatedTasks[cancelIndex];
                cancelIndex = (cancelIndex + 1) % allocatedTasks.length;
            } while(tasksToCancel.contains(cancel) && cancelIndex != startIndex);
            cpus -= cancel.getSlo();
            tasksToCancel.add(cancel);
        }
        return tasksToCancel;
    }
}

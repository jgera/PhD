package edu.upc.mario.trustsim.wl;

import edu.upc.mario.trustsim.Const;
import edu.upc.mario.trustsim.Provider;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Class representing a physical node
 */
public class Node  {

    private Provider provider;

    private Set<Task> allocatedTasks = new HashSet<Task>();
    private int allocatedResources = 0;

    private static final Random rnd = new Random(System.currentTimeMillis());


    public Node(Provider provider) {
        this.provider = provider;
    }

    /**
     * Returns false if task does not fit in the resources. However, it doesn't mean
     * that it can not be allocated. Providers that oversell resources could add it
     * @param task
     * @return
     */
    public boolean isTaskAllocatable(Task task) {
        return task.getSlo() + allocatedResources <= getCpus();
    }

    public void allocateTask(Task task) {
        assert !allocatedTasks.contains(task);
        allocatedResources += task.getSlo();
        assert allocatedResources <= getCpus();
        allocatedTasks.add(task);
    }

    /**
     * Cancels or deallocates a task
     */
    public void deallocateTask(Task task) {
        boolean removed =allocatedTasks.remove(task);
        assert removed;
        allocatedResources -= task.getSlo();
    }

    private static final double WORKLOAD_UNCERTAINTY = 0.15;
    public double getLoad() {
        double rate = Math.abs(rnd.nextGaussian());
        double variation = allocatedResources * rate * WORKLOAD_UNCERTAINTY;
        return ((allocatedResources + variation)  / getCpus());
    }

    /**
     *
     * @param time
     * @return a vector: 0 --> fulfilled SLAs, 1 --> SLAs taken into account;
     */
    public double[] step(long time) {

        // deallocates tasks
        Set<Task> deallocatable = new HashSet<Task>();
        for(Task t : allocatedTasks) {
            if(t.getStartTime() + t.getDuration() <= time) {
                deallocatable.add(t);
            }
        }
        for(Task t : deallocatable) {
            deallocateTask(t);
            t.getClient().updateTrust(t,provider.getIdentifier());
            provider.addRevenue(t.getRevenue());
        }

        // checks if node is overloaded and, in case it is, chooses what SLAs to violate
        double load = getLoad();
        double checkedTasks = 0;
        double fulfilledSLAs = 0;

        Set<Task> tasksToCancel = provider.selectSLAsToViolate(this, load, time);
        penaltiesIncrement = 0;
        for(Task t : allocatedTasks) {
            t.accumulateQoS(!tasksToCancel.contains(t));
            checkedTasks++;
            fulfilledSLAs += t.getSLAFulfillmentRateIncrement(time);
            penaltiesIncrement += t.getPenaltyIncrement(time);

        }

        return new double[] { fulfilledSLAs, checkedTasks};
    }

    public void migrate(Task t, Node destinationNode) {
        deallocateTask(t);
        destinationNode.allocateTask(t);
    }

    private double penaltiesIncrement;
    public double getAllPenaltiesIncrement(long time) {
        return penaltiesIncrement;
    }

    public int getFreeResources() {
        return getCpus() - allocatedResources;
    }

    public int getCpus() {
        return provider.isFailure() ? (int) Math.round(Const.NODE_MAX_CPUs * provider.getFailurePercentage()) : Const.NODE_MAX_CPUs;
    }

    public Task[] getAllocatedTasks() {
        Task[] tasks = new Task[allocatedTasks.size()];
        return allocatedTasks.toArray(tasks);
    }
}

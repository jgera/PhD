package edu.upc.mario.trustsim;

import edu.upc.mario.trustsim.tasks.TaskCanceler;
import edu.upc.mario.trustsim.wl.Node;
import edu.upc.mario.trustsim.wl.Task;

import java.util.*;


public class Provider {
    public final int NODES_NUM = 8;

    private String identifier;
    private Set<Node> nodes;
    private double revenue = 0;
    private TaskCanceler canceler = null;
    private boolean failure = false;

    public Provider(String identifier) {
        this.identifier = identifier;
        nodes = new HashSet<Node>(NODES_NUM);
        for(int i = 0 ; i < NODES_NUM ; i++) {
            Node n = new Node(this);
            nodes.add(n);
        }
    }

    private static final long FAILURE_START =1800;
    private static final long FAILURE_DURATION =300;
    private static final double FAILURE_PERCENTAGE =0.2;

    public double getFailurePercentage() {
        return FAILURE_PERCENTAGE;
    }

    private double[] fulfillmentHistory = new double[Const.FULFILLMENT_WINDOW_LENGTH];

    public void setTaskCanceler(TaskCanceler canceler) {
        this.canceler = canceler;
    }

    private double[] revenueHistory = new double[Const.REVENUE_WINDOW_LENGTH];

    public double getPrice(Task t) {
        final double MAX_PRICE = 1.5;
        final double MIN_PRICE = 0.9;
        double minPrice = MAX_PRICE;
        for(Node n : nodes) {
            double p = (double)n.getFreeResources()/(double)n.getCpus();
            p = MIN_PRICE + p * (MAX_PRICE-MIN_PRICE);
            if(p < MIN_PRICE) {
                p = MIN_PRICE;
            } else if(p > MAX_PRICE) {
                p = MAX_PRICE;
            }
            if(p < minPrice) {
                minPrice = p;
            }
        }
        return minPrice * t.getSlo();
    }

    public String getIdentifier() {
        return identifier;
    }

    public double getRevenue() {
        return revenue;
    }

    public void addRevenue(double revenue) {
        this.revenue += revenue;
    }

    private double stepPenalty;

    public void step(long time) {
        stepPenalty = 0;
        double fulfilledSLAs = 0, accountedSLAs = 0;
        failure = time >= FAILURE_START && time < FAILURE_START + FAILURE_DURATION;

        for(Node n : nodes) {
            double[] fulfillment = n.step(time);
            stepPenalty += n.getAllPenaltiesIncrement(time);
            fulfilledSLAs += fulfillment[0];
            accountedSLAs += fulfillment[1];
        }
        if(accountedSLAs == 0) {
            stepFulfillmentRate = 1;
        } else {
            stepFulfillmentRate = fulfilledSLAs / accountedSLAs;
        }

        fulfillmentHistory[(int)time% fulfillmentHistory.length] = stepFulfillmentRate;

        revenueHistory[(int)time%revenueHistory.length] = revenue;
    }

    public boolean isFailure() {
        return failure;
    }

    //    public double getAverageFulfillmentRate(long time) {
//        double sum = 0;
//        for(int i = 0 ; i < fulfillmentHistory.length ; i++) {
//            sum += fulfillmentHistory[(int)(time+i) % fulfillmentHistory.length];
//        }
//        return sum/(double)fulfillmentHistory.length;
//    }

    public double getRevenueIncrementAtWindow(long time) {
        int now = (int)time % revenueHistory.length;
        int windowStart = (int) (time + 1) % revenueHistory.length;
        return revenueHistory[now] - revenueHistory[windowStart];
    }

    private static final Random RND = new Random(System.currentTimeMillis());


    public double getWorkloadPercentage() {
        double usedResources = 0;
        double totalResources = 0;
        for(Node n : nodes) {
            usedResources += n.getCpus()  - n.getFreeResources();
            totalResources += n.getCpus();
        }
        return usedResources / totalResources;
    }

    public double getStepPenalty() {
        return stepPenalty;
    }

    private double stepFulfillmentRate;
    public double getStepFulfillmentRate() {
        return stepFulfillmentRate;
    }

    /**
     * Returns true if the task has been allocated. False if not enough free resources.
     * @param t
     * @return
     */
    public boolean allocateTask(Task t) {
        // policy: allocate it to the node with more free resources (MAX SLA)
        // todo: consider consolidation (eco efficiency) or maximum reputation
        Node n = getBestFittingNode(t);
        if(n != null) {
            n.allocateTask(t);
            return true;
        } else {
            return false;
        }

    }

    public boolean isTaskFitting(Task t) {
        return getBestFittingNode(t) != null;
    }

    private Node getBestFittingNode(Task t) {
        List<Node> sortedNodes = new LinkedList<Node>(nodes);
        Collections.sort(sortedNodes, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o2.getFreeResources() - o1.getFreeResources();
            }
        });
        Node n = sortedNodes.get(0);
        if(n.getFreeResources() >= t.getSlo()) {
            return n;
        } else {
            return null;
        }
    }

    public Set<Task> selectSLAsToViolate(Node node, double load, long time) {
        return canceler.selectSLAsToViolate(node, load, time);
    }
}

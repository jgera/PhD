package edu.upc.mario.trustsim.wl;

import edu.upc.mario.trustsim.Client;

public class Task {
    private double slo;
    private int startTime;
    private int duration;
    private Client client;
    private int violationDuration = 0;
    private double maxRevenue, maxPenalty;
    private double lastRevenue, beforeLastRevenue;
    private long lastRevenueQueryTime = 0;

    // according to my research: http://upcommons.upc.edu/e-prints/bitstream/2117/15943/1/policydescription.pdf
    // but with a modification: instead of configuring MAX revenue and MAX penalty,, i consider 1..0 range
    // (100% fulfillment - 0% fulfillment)
    // these values are not absolutes but percentages of time over the total duration
    private final static double MRT = 0.15, MPT = 0.30;

    public Task(Client client, double slo, int startTime, int duration) {
        this.slo = slo;
        this.startTime = startTime;
        this.duration = duration;
        this.client = client;
    }


    private static final double PENALTY_PROPORTION_PER_REVENUE = -1.5;
    public void setRevenueFunction(double maxRevenue) {
        this.maxRevenue = this.lastRevenue = this.beforeLastRevenue = maxRevenue;
        this.maxPenalty = PENALTY_PROPORTION_PER_REVENUE * maxRevenue;
    }

    public double getSlo() {
        return slo;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void accumulateQoS(boolean slaFulfilled) {
        if(!slaFulfilled) {
            violationDuration++;
        }
    }

    public double getRevenueIfCancelledAtTime(long time) {
        double vd = violationDuration + startTime + duration - time;
        double dd = duration;
        double fulfillment;
        if(vd < dd * MRT) {
            fulfillment = 1;
        } else if(vd > dd * MPT) {
            fulfillment = 0;
        } else {
            fulfillment = 1-(((double)vd) - dd * MRT) / (dd * (MPT - MRT));
        }
        return (maxPenalty - maxRevenue) * (1-fulfillment) + maxRevenue;
    }

    public double getMaxRevenue() {
        return maxRevenue;
    }

    long lastSlaFulQueryTime=0;
    double beforeLastFulfillmentRate = 1;
    double lastFulfillmentRate = 1;
    public double getSLAFulfillmentRateIncrement(long time) {
        assert time >= lastSlaFulQueryTime;
        if(time > lastSlaFulQueryTime) {
            beforeLastFulfillmentRate = lastFulfillmentRate;
            lastFulfillmentRate = getSLAFulfillmentRate();
        }
        return lastFulfillmentRate - beforeLastFulfillmentRate;
    }

    public double getSLAFulfillmentRate() {
        double dd = duration;

        if(violationDuration < dd * MRT) {
            return 1;
        } else if(violationDuration > dd * MPT) {
            return 0;
        } else {
            return 1-(((double)violationDuration) - dd * MRT) / (dd * (MPT - MRT));
        }
    }
    public double getRevenue() {
        return (maxPenalty - maxRevenue) * (1-getSLAFulfillmentRate()) + maxRevenue;
    }

    public double getPenaltyIncrement(long time) {
        assert time >= lastRevenueQueryTime;
        if (time > lastRevenueQueryTime) {
            beforeLastRevenue = lastRevenue;

            lastRevenue = getRevenue();
        }
        return lastRevenue - beforeLastRevenue;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public String toString() {
        return "violationDuration: " + violationDuration + ", fulfillmentPercentage: " + (getSLAFulfillmentRate()*100) +"%, revenue: " + getRevenue();
    }
}

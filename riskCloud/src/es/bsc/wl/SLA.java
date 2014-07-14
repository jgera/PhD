/*
    Author: Mario Macias, 2013

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License Version 2, as
    published by the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>
*/
package es.bsc.wl;

import es.bsc.risk.AbstractNode;

import java.util.HashMap;
import java.util.Map;
import static es.bsc.Constants.*;

public class SLA implements Cloneable {
    public enum Policy {COST_MIN, NODE_RISK_MIN, GRAPH_RISK_MIN};
    private double price;
    // a negative number that multiplies the price for specifying the penalty for not fulfilling the SLA
    protected Map<String, Type[]> allocation;

    // how much time the SLA was under violation
    private double violationDuration=0;
    AbstractNode headNode;

    private final int startTime, duration;

    private Policy policy;

    public SLA(Policy policy, AbstractNode headNode, int startTime, int duration) {
        this.headNode = headNode;
        this.startTime = startTime;
        this.duration = duration;
        this.policy = policy;
        double tru = 0;
        for(Type t : headNode.getConnectedTypes()) {
            tru += t.getResourceUnits() * duration;
        }
        totalResourceUnitsHour = tru;
    }

    public double getPrice() {
        return price;
    }

    private final double totalResourceUnitsHour;
    public double getTotalResourceUnitsHour() {
        return totalResourceUnitsHour;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void notifyViolation(double hourlyDuration) {
        violationDuration += hourlyDuration;
    }

    public AbstractNode getHeadNode() {
        return headNode;
    }

    public Map<String, Type[]> getAllocation() {
        return allocation;
    }

    public void setAllocation(Map<String, Type[]> allocation) {
        this.allocation = allocation;
    }

    public double getResourceUnitsSum() {
        double sum = 0;
        for(Type t : headNode.getConnectedTypes()) {
            sum += t.getResourceUnits();
        }
        return sum;
    }

    public int getStartTime() {
        return startTime;
    }
    public int getDuration() {
        return duration;
    }

    public double getRevenue() {
        int policyIndex = policy.ordinal();
        double mrt = duration*MRT[policyIndex];
        double mpt = duration * MPT[policyIndex];
        if(violationDuration < mrt) {
            return price;
        } else if(violationDuration > mpt) {
            return price * MP[policyIndex];
        } else {
            double dur = violationDuration - mrt;
            return price + ((dur / (mpt-mrt)) * (MP[policyIndex]-1) * price);
        }
    }

    public double getPenaltyPercentage() {
        int policyIndex = policy.ordinal();
        double mrt = duration*MRT[policyIndex];
        double mpt = duration * MPT[policyIndex];
        if(violationDuration < mrt) {
            return 0;
        } else if(violationDuration > mpt) {
            return 1;
        } else {
            double dur = violationDuration - mrt;
            return dur / (mpt-mrt);
        }
    }


    @Override
    public Object clone() {
        SLA sla = new SLA(policy,(AbstractNode)headNode.clone(), startTime, duration);
        sla.violationDuration = violationDuration;
        sla.price = price;

        if(allocation != null) {
            sla.allocation = new HashMap<String, Type[]>();
            for(String k : allocation.keySet()) {
                Type[] src = allocation.get(k);
                Type[] dst = new Type[src.length];
                for(int i = 0 ; i < dst.length ; i++) {
                    dst[i] = (Type) src[i].clone();
                }
                sla.allocation.put(k,dst);
            }
        }

        return sla;
    }

}

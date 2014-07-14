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

import java.util.*;
import static es.bsc.Constants.*;

public class Resource {
    public enum HostedType { STORAGE, COMPUTE };
    public HostedType hostedType = HostedType.COMPUTE;

    private String id;

    protected double maxResourceUnits;
    protected AccountingGroup accountingGroup;

    protected double[] workloadData;

    protected final double TCO = DEFAULT_TCO;
    private Provider provider;

    public Resource(Provider p, String id, int maxResourceUnits) {
        this.maxResourceUnits = maxResourceUnits;
        workloadData = new double[AccountingGroup.DEFAULT_MAX_LIFE_TIME];
        this.id = id;
        provider = p;
    }

    public Resource(Provider p, String id, HostedType hostedType,int maxResourceUnits) {
        this.maxResourceUnits = maxResourceUnits;
        this.hostedType = hostedType;
        workloadData = new double[AccountingGroup.DEFAULT_MAX_LIFE_TIME];
        this.id = id;
        provider = p;
    }

    public String getId() {
        return id;
    }

    public Provider getProvider() {
        return provider;
    }

    private Map<String,Type> runningTypes = new HashMap<String, Type>();

    public void addRunningType(Type t) {
        if(runningTypes.get(t.getId()) == null) {
            int d = t.getDuration();
            int s = t.getStartTime();
            double r = t.getResourceUnits();
            for(int i = 0 ; i < d ; i++) {
                workloadData[s - accountingGroup.insertionTime + i] += r;
                // todo: remove this when doing overprovisioning
                try {
                    assert workloadData[s - accountingGroup.insertionTime + i] <= maxResourceUnits;
                } catch(AssertionError e) {
                    System.out.println("maxResourceUnits = " + maxResourceUnits);
                    System.out.println("s - accountingGroup.insertionTime + i = " + (s - accountingGroup.insertionTime + i));
                    System.out.println("workloadData[s - accountingGroup.insertionTime + i] = " + workloadData[s - accountingGroup.insertionTime + i]);
                    throw e;
                }

            }
            runningTypes.put(t.getId(),t);
            t.setOwningResource(this);
        }
    }

    public void removeRunningType(Type t) {
        if(runningTypes.get(t.getId()) != null) {
            int d = t.getDuration();
            int s = t.getStartTime();
            double r = t.getResourceUnits();
            for(int i = 0 ; i < d ; i++) {
                workloadData[s + i - accountingGroup.insertionTime] -= r;
                try {
                    assert workloadData[s - accountingGroup.insertionTime + i] >= 0;
                } catch(AssertionError e) {
                    System.out.println(workloadData[s - accountingGroup.insertionTime + i]);
                    throw e;
                }
            }
            runningTypes.remove(t.getId());
        }
    }
    public Type removeRunningType(String id) {
        Type t = runningTypes.remove(id);
        removeRunningType(t);
        return t;
    }

    public boolean isTypeFitting(Type t) {
        return isTypesFitting(new Type[] {t} );
    }

    public boolean isTypesFitting(Type[] types) {
        // We assume all the types start and end at the same time
        int d = types[0].getDuration();
        int s = types[0].getStartTime();
        if(s + d > accountingGroup.maxLifeTime) {
            return false;
        }
        double r = 0;
        for(Type t : types) {
            assert !runningTypes.containsKey(t.getId());
            r += t.getResourceUnits();
        }
        for(int i = 0 ; i < d ; i++) {
            if(workloadData[s + i - accountingGroup.insertionTime] + r > maxResourceUnits) {
                return false;
            }
        }
        return true;
    }

    public Type[] getRunningTypes() {
        return runningTypes.values().toArray(new Type[runningTypes.size()]);
    }

    /**
     *
     * @param currentTime current time in hours
     */
    public void step(int currentTime) {
        accountingGroup.currentTime = currentTime;
        for(Type t : getRunningTypes()) {
            if(t.getStartTime() + t.getDuration() > currentTime) {
                runningTypes.remove(t.getId());
            }
        }
    }

    public AccountingGroup getAccountingGroup() {
        return accountingGroup;
    }

    /**
     * returns risk in function of the PoF function and the current occupation
     * @return
     */
    public double getRisk(int startTime, int duration, Set<Type> allTypes, boolean multiResourceType) {
        // if resource do not fit, risk == 1
        if(startTime+duration >= accountingGroup.insertionTime + accountingGroup.maxLifeTime) {
            return 1;
        }


        double overload = 0;
        if(allTypes != null) {
            for(Type t : allTypes) {
                if(!multiResourceType || t.getOwningResource() == this) {
                    overload += t.getResourceUnits();
                }
            }
        }
        double allResources = 0;
        for(int i = startTime - accountingGroup.insertionTime ;
            i < startTime - accountingGroup.insertionTime + duration ; i++) {
            allResources += workloadData[i] + overload;
        }
        double workloadRisk = allResources / (maxResourceUnits * duration);
        // the PoF is the highest value of the PoF function during the interval
        // todo: try with average values
        double resourceRisk = 0;
        for(int i = startTime ; i < startTime + duration ; i++) {
            double current = accountingGroup.getPoFFunction(i - accountingGroup.insertionTime);
            if(resourceRisk < current) {
                resourceRisk = current;
            }
        }
        return RESOURCE_RISK_WEIGHT * resourceRisk + WORKLOAD_RISK_WEIGHT * workloadRisk;
    }
}

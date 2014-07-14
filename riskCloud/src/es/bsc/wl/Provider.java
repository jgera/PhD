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

import es.bsc.Constants;
import es.bsc.econ.Pricer;
import es.bsc.econ.RiskPricer;
import es.bsc.risk.Node;
import es.bsc.util.Output;
import es.bsc.util.Stat;
import sun.nio.cs.CharsetMapping;

import java.security.Policy;
import java.util.*;

public class Provider {

    private Pricer pricer;

    // Resources are ordered from newer to older
    private Map<String, Resource> resources = new TreeMap<String, Resource>();
    private Map<String, AccountingGroup> accountingGroups = new HashMap<String, AccountingGroup>();

    List<SLA> allSLAs = new ArrayList<SLA>();


    private static final int WL_HISTORY_LENGTH = 7 * 24; // 1 week history
    private double[] workloadHistory = new double[WL_HISTORY_LENGTH];

    // SLAs are ordered by endTime (ascending)
    Comparator slaComparator = new Comparator<SLA>() {
        @Override
        public int compare(SLA o1, SLA o2) {
            return (o1.getStartTime()+o1.getDuration()) - (o2.getStartTime()+o2.getDuration());
        }
    };

    public Provider() {
        pricer = new RiskPricer(this);
        //initialize wl history at 43%, as measured in previous tests
        for(int i = 0 ; i < WL_HISTORY_LENGTH ; i++) {
            workloadHistory[i] = 0.43;
        }
        historicWorkload = 0.43;

    }

    public int getActiveResources() {
        return resources.size();
    }

    double sumOfResourceUnits = 0;
    public double getSumOfResourceUnits() {
        return sumOfResourceUnits;
    }

    int addedResources = 0;
    public int getAddedResources() {
        return addedResources;
    }

    private double lastStepPenaltyPerCPUHour;
    public void step(int currentTime) {
        // remove SLAs that finished
        deallocatedSLAs.clear();
        for(SLA s : allSLAs) {
            if(currentTime > s.getStartTime() + s.getDuration()) {
                deallocatedSLAs.add(s);
            }
        }

        for(SLA s : deallocatedSLAs) {
            deallocateSLA(s);
        }

        // remove resources that ended its life time
        Collection<AccountingGroup> toRemove = new HashSet<AccountingGroup>();
        for(AccountingGroup a : accountingGroups.values()) {
            a.currentTime = currentTime;
            Iterator<Resource> ri = a.getResourcesIterator();
            while(ri.hasNext()) {
                ri.next().step(currentTime);
            }

            if(currentTime > a.insertionTime + a.maxLifeTime) {
                toRemove.add(a);
            }
        }
        for(AccountingGroup a : toRemove) {
            removeAccountingGroup(a);
        }
        workloadHistory[currentTime % WL_HISTORY_LENGTH] = getActualWorkload(currentTime);
        historicWorkload = 0;
        for(double d : workloadHistory) {
            historicWorkload += d;
        }
        historicWorkload /= WL_HISTORY_LENGTH;
        assert historicWorkload >= 0;
        assert historicWorkload <= 1;

        // check sla violations
        for(SLA s : allSLAs) {
            double pof = s.getHeadNode().getPropagatedPoF();
            if(Constants.RND.nextDouble() < pof) {
                s.notifyViolation(Math.min(pof + Math.abs(pof * Constants.RND.nextGaussian()), 1));
            }
        }
    }

    private double historicWorkload;
    public double getHistoricWorkload() {
        return historicWorkload;
    }


    public void addAccountingGroup(AccountingGroup g) {
        accountingGroups.put(g.getId(),g);
        Iterator<Resource> it = g.getResourcesIterator();
        while(it.hasNext()) {
            Resource r = it.next();
            resources.put(r.getId(),r);
            addedResources++;
        }
        sumOfResourceUnits += g.totalResourceUnits;
    }

    public void removeAccountingGroup(AccountingGroup g) {
        Iterator<Resource> it = g.getResourcesIterator();
        while(it.hasNext()) {
            Resource r = it.next();
            resources.remove(r.getId());
        }
        accountingGroups.remove(g.getId());
        sumOfResourceUnits -= g.totalResourceUnits;
    }

    /**
     *
     * @param slaTemplate the SLA template
     * @return a clone of the SLA template with a price and an allocation proposal. Returns null if no sla has been allocated
     */
    public SLA checkSLAAllocation(SLA slaTemplate) {
        SLA alloc = null;
        if(slaTemplate.getPolicy() == SLA.Policy.NODE_RISK_MIN || slaTemplate.getPolicy() == SLA.Policy.GRAPH_RISK_MIN) {
            alloc = checkSLAAllocationRiskMin(slaTemplate);
        } else {
            alloc =  checkSLAAllocationCostMin(slaTemplate);
        }
        if(alloc != null) {
            // establishes owningResources
            for(String resId : alloc.getAllocation().keySet()) {
                Resource res = resources.get(resId);
                Type[] allocsForRes = alloc.getAllocation().get(resId);
                for(Type t : allocsForRes) {
                    t.setOwningResource(res);
                }
            }

            pricer.calculatePrice(alloc);
        }

        return alloc;
    }

    public SLA checkSLAAllocationRiskMin(SLA slaTemplate) {
        SLA slaAllocation = (SLA)slaTemplate.clone();
        // this is a Risk Minimization policy because tend to distribute within resources with less risk
        Map<String, List<Type>> allocations = new HashMap<String, List<Type>>();
        for(String resId : resources.keySet()) {
            allocations.put(resId,new ArrayList<Type>());
        }

        Set<Type> allTypes = slaAllocation.getHeadNode().getConnectedTypes();

        // type taken for calculating startTime and duration
        // we assume all types have the same start time and duration
        Type someType = allTypes.iterator().next();
        int startTime = someType.getStartTime();
        int duration = someType.getDuration();

        List<Resource> orderedResources = new ArrayList<Resource>();
        orderedResources.addAll(resources.values());

        Iterator<Type> typeIterator = allTypes.iterator();
        while(typeIterator.hasNext()) {
            Type typeToAllocate = typeIterator.next();

            RiskMinComparator comp = new RiskMinComparator(startTime,duration, allocations);
            Collections.sort(orderedResources, comp);
            Iterator<Resource> resIter = orderedResources.iterator();
            Resource res = null;
            Type[] types = null;
            boolean toContinue;
            boolean fits;
            do {
                res = resIter.next();
                List<Type> resAlloc = allocations.get(res.getId());
                types = resAlloc.toArray(new Type[resAlloc.size()+1]);
                types[types.length-1] = typeToAllocate;
                fits = res.isTypesFitting(types);
                toContinue = !fits && resIter.hasNext();
            } while(toContinue);

            if(fits) {
                allocations.get(res.getId()).add(typeToAllocate);
            } else {
                return null;
            }

        }

        Map<String,Type[]> arrayAllocations = new HashMap<String, Type[]>();
        for(String resId : allocations.keySet()) {
            List<Type> alloc = allocations.get(resId);
            if(alloc.size() > 0) {
                arrayAllocations.put(resId,alloc.toArray(new Type[alloc.size()]));
            }
        }

        slaAllocation.setAllocation(arrayAllocations);
        return slaAllocation;
    }
    public SLA checkSLAAllocationCostMin(SLA slaTemplate) {
        SLA slaAllocation = (SLA) slaTemplate.clone();
        // this is a eco maximization policy because consolidates.

        Map<String, Type[]> allocations = new HashMap<String, Type[]>();

        // check if the SLA fits this is an eco efficiency
        Set<Type> allTypes = slaAllocation.getHeadNode().getConnectedTypes();

        // we assume all types have the same start time and duration
        Type someType = allTypes.iterator().next();
        List<Resource> orderedResources = new ArrayList<Resource>();
        orderedResources.addAll(resources.values());
        Collections.sort(orderedResources,new CostMinComparator(someType.getStartTime(),someType.getDuration()));

        Iterator<Resource> resourceIterator = orderedResources.iterator();
        Iterator<Type> typeIterator = allTypes.iterator();
        Resource r = resourceIterator.next();
        List<Type> typesForAResource = new ArrayList<Type>();

        while(typeIterator.hasNext()) {
            Type t = typeIterator.next();
            typesForAResource.add(t);

            while(!r.isTypesFitting(typesForAResource.toArray(new Type[typesForAResource.size()]))) {
                typesForAResource.remove(t);
                if(resourceIterator.hasNext()) {
                    if(typesForAResource.size() > 0) {
                        allocations.put(r.getId(),typesForAResource.toArray(new Type[typesForAResource.size()]));
                        typesForAResource = new ArrayList<Type>();
                    }
                    typesForAResource.add(t);
                    r = resourceIterator.next();

                } else {
                    return null; // not all types fit
                }
            }
        }
        if(typesForAResource.size() > 0) {
            allocations.put(r.getId(),typesForAResource.toArray(new Type[typesForAResource.size()]));
        }

        slaAllocation.setAllocation(allocations);
        return slaAllocation;
    }



    /**
     *
     * @param s
     * @return
     */
    public void allocateSLA(SLA s) {
        Map<String,Type[]> typeAllocations = s.getAllocation();
        assert s.getHeadNode().getConnectedTypes().size() > 0;
        double amortizedPortion = s.getPrice() / s.getHeadNode().getConnectedTypes().size();

        for(String resId : typeAllocations.keySet()) {
            Resource r = resources.get(resId);
            Type[] types = typeAllocations.get(resId);
            assert r.isTypesFitting(types);
            for(Type t : types) {
                r.addRunningType(t);
                r.accountingGroup.totalAmortized += amortizedPortion;
            }
        }
        allSLAs.add(s);

    }

    private Set<SLA> deallocatedSLAs = new HashSet<SLA>();
    public void deallocateSLA(SLA s) {
        for(Type t : s.getHeadNode().getConnectedTypes()) {
            t.getOwningResource().removeRunningType(t);
        }
        allSLAs.remove(s);
    }

    @Override
    public String toString() {
        String ret = "";
        for(Resource r : resources.values()) {
            ret += r.toString() +"\n";
        }
        return ret;
    }

    /**
     *
     * @return Index 0 is average. Index 1 is stdev
     */
    public double[] getAverageRisk() {
        return getAverageRisk(null);
    }
    public double[] getAverageRisk(SLA.Policy filter) {
        double avg = 0, stdev = 0;
        Stat st = new Stat();
        for(SLA s : allSLAs) {
            if(filter == null || filter == s.getPolicy()) {
                st.addValue(s.getHeadNode().getPropagatedPoF());
            }
        }
        return st.getStats();
    }

    public double[] getAvgAgeofResources(int time) {
        double[] sum = new double[SLA.Policy.values().length];
        double[] all = new double[SLA.Policy.values().length];
        for(SLA s : allSLAs) {
            for(Type t : s.getHeadNode().getConnectedTypes()) {
                int idx = s.getPolicy().ordinal();
                AccountingGroup ag = t.getOwningResource().getAccountingGroup();
                double age = time - ag.insertionTime;
                sum[idx] += age;
                all[idx]++;
            }
        }
        for(int i = 0 ; i < sum.length ; i++) {
            sum[i] /= all[i];
        }
        return sum;
    }

    public double[] getAvgPenaltyPerSLA() {
        double[] sum = new double[SLA.Policy.values().length];
        double[] all = new double[SLA.Policy.values().length];
        for(SLA s : deallocatedSLAs) {
            int idx = s.getPolicy().ordinal();
            sum[idx] += s.getPenaltyPercentage();
            all[idx]++;
        }
        for(int i = 0 ; i < sum.length ; i++) {
            if(all[i] > 0) {
                sum[i] /= all[i];
            }
        }
        return sum;
    }
    public double[] getAvgNetBenefitPerResUnit() {
        double[] sum = new double[SLA.Policy.values().length];
        double[] all = new double[SLA.Policy.values().length];
        for(SLA s : allSLAs) {
            int idx = s.getPolicy().ordinal();
            double totalReservationPrice = 0;
            for(Type t : s.getHeadNode().getConnectedTypes()) {
                totalReservationPrice += t.getReservationPrice();
            }
            sum[idx] += s.getRevenue() - totalReservationPrice;
            all[idx] += s.getTotalResourceUnitsHour();
        }
        for(int i = 0 ; i < sum.length ; i++) {
            sum[i] /= all[i];
        }
        return sum;
    }

    public double[] getAvgRevenuePerResUnit() {
        double[] sum = new double[SLA.Policy.values().length];
        double[] all = new double[SLA.Policy.values().length];
        for(SLA s : allSLAs) {
            int idx = s.getPolicy().ordinal();
            sum[idx] += s.getRevenue();
            all[idx] += s.getTotalResourceUnitsHour();
        }
        for(int i = 0 ; i < sum.length ; i++) {
            sum[i] /= all[i];
        }
        return sum;
    }

    public double[] getAvgPricePerResUnit() {
        double[] sum = new double[SLA.Policy.values().length];
        double[] all = new double[SLA.Policy.values().length];
        for(SLA s : allSLAs) {
            int idx = s.getPolicy().ordinal();
            sum[idx] += s.getPrice();
            all[idx] += s.getTotalResourceUnitsHour();
        }
        for(int i = 0 ; i < sum.length ; i++) {
            sum[i] /= all[i];
        }
        return sum;
    }

    private int lastActWlTime = -1;
    private double lastWl;
    public double getActualWorkload(int time) {
        if(time != lastActWlTime) {
            double allUnits = 0;
            double workLoad = 0;
            for(Resource r : resources.values()) {
                if(time >= r.accountingGroup.insertionTime
                        && time < r.accountingGroup.insertionTime + r.accountingGroup.maxLifeTime) {
                    allUnits += r.maxResourceUnits;
                    workLoad += r.workloadData[time - r.accountingGroup.insertionTime];
                }
            }
            lastActWlTime = time;
            lastWl = workLoad / allUnits;
        }
        return lastWl;
    }

    public void printAllNodesWorkload(int time) {
        StringBuilder sb = new StringBuilder();
        for(Resource r : resources.values()) {
            double wl = r.workloadData[time - r.accountingGroup.insertionTime] / r.maxResourceUnits;
            sb.append(wl).append(" ");
        }
        Output.NODESWL.println(sb.toString());
    }

    public double getAggressivenessFactor(int startTime, int duration) {

        double workload = 0;
        for(int time = startTime ; time < startTime + duration ; time++) {
            workload += WorkloadGenerator.getWorkloadPercentage(time);
        }
        return workload / duration;
    }

    /**
     * Compares by consolidation and amortization of the resource
     * (the more amortized is the AccountingGroup, the cheaper)
     */
    private static class CostMinComparator implements Comparator<Resource> {
        int startTime, duration;

        private static final double CONSOLIDATION_WEIGHT = 0.5;
        private static final double AMORTIZATION_WEIGHT = 1-CONSOLIDATION_WEIGHT;
        private CostMinComparator(int startTime, int duration) {
            this.startTime = startTime;
            this.duration = duration;
        }

        @Override
        public int compare(Resource o1, Resource o2) {

            double consolidation1 = 0, consolidation2 = 0;
            for(int i = startTime ; i < startTime + duration ; i++) {
                consolidation1 += i-o1.accountingGroup.insertionTime < o1.workloadData.length ?
                        o1.workloadData[i-o1.accountingGroup.insertionTime]
                        : o1.maxResourceUnits;
                consolidation2 += i-o2.accountingGroup.insertionTime < o2.workloadData.length ?
                        o2.workloadData[i-o2.accountingGroup.insertionTime]
                        : o2.maxResourceUnits;
            }
            consolidation1 /= (o1.maxResourceUnits * duration);
            consolidation2 /= (o2.maxResourceUnits * duration);

            double amortization1 = o1.accountingGroup.getAmortizationPercentage();
            double amortization2 = o2.accountingGroup.getAmortizationPercentage();

            double score1 = consolidation1 * CONSOLIDATION_WEIGHT + amortization1 * AMORTIZATION_WEIGHT;
            double score2 = consolidation2 * CONSOLIDATION_WEIGHT + amortization2 * AMORTIZATION_WEIGHT;

            if(score1 < score2) {
                return 1;
            } else if(score2 < score1) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private class RiskMinComparator implements Comparator<Resource> {
        int startTime, duration;
        Map<String,List<Type>> candidateAllocations;

        private RiskMinComparator(int startTime, int duration, Map<String,List<Type>> candidateAllocations) {
            this.startTime = startTime;
            this.duration = duration;
            this.candidateAllocations = candidateAllocations;
        }

        @Override
        public int compare(Resource o1, Resource o2) {
            Set<Type> overload1 = null, overload2 = null;

            if(candidateAllocations.get(o1.getId()) != null) {
                overload1 = new HashSet<Type>();
                overload1.addAll(candidateAllocations.get(o1.getId()));
            }
            if(candidateAllocations.get(o2.getId()) != null) {
                overload2 = new HashSet<Type>();
                overload2.addAll(candidateAllocations.get(o2.getId()));
            }

            double r1 = o1.getRisk(startTime,duration,overload1,false);
            double r2 = o2.getRisk(startTime,duration,overload2,false);
            if(r1 < r2) {
                return -1;
            } else if(r1 > r2) {
                return 1;
            } else {
                return 0;
            }
        }
    }



}

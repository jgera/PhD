/**
 * Copyright (C) 2010-2012 Barcelona Supercomputing Center
 *
 * This library is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.optimis.hm.simul.emotive;

import eu.optimis.hm.simul.Simulator;
import eu.optimis.hm.simul.ipvm.HolisticManager;
import eu.optimis.hm.simul.tools.MathUtils;
import eu.optimis.hm.simul.trec.TrecApi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PhysicalNode {
    public static final int CPUs = 32;
    private int usedCPUs = 0;

    private int accumulatedRisk = 0;

    private boolean failure = false;

    private String nodeId;

    private Map<String, VirtualMachine> vms = new HashMap<String, VirtualMachine>();
    private TrecApi trec;

    public PhysicalNode(String nodeId, TrecApi trec) {
        this.nodeId = nodeId;
        this.trec = trec;
    }

    public void setProbabilityOfFailure(double probabilityOfFailure) {
        this.probabilityOfFailure = probabilityOfFailure;
    }

    public void deploy(VirtualMachine vm) {
        assert !vms.keySet().contains(vm.getId());
        assert isFitting(vm);
        vms.put(vm.getId(),vm);
        usedCPUs += vm.getCpus();
    }

    public VirtualMachine getVirtualMachine(String vmId) {
        return vms.get(vmId);
    }

    // calls to undeploy. Marks VM to shutdown
    public void undeploy(VirtualMachine vm) {
        assert vms.containsKey(vm.getId());
        vm.setStatus(VirtualMachine.Status.SHUTDOWN);
    }

    public void undeploy(String vmId) {
        vms.get(vmId).setStatus(VirtualMachine.Status.SHUTDOWN);
    }

    // removes from the list
    public void remove(VirtualMachine vm) {
        assert vms.containsKey(vm.getId());
        vms.remove(vm.getId());
        usedCPUs -= vm.getCpus();
    }

    public String getNodeId() {
        return nodeId;
    }

    public Collection<VirtualMachine> getAllVMs() {
        return vms.values();
    }

    private static final double MAX_FAILURE_RATE = 4*3600;
    private static final long RESTART_DURATION = 10 * 60;
    private static final long MAX_TIME_TO_FAIL = 5*60;

    private long failureTime = 0;
    private double probabilityOfFailure = 0;

    private int numberOfFailures = 0;

    // Parameter "canFail" : let's limit the failure of nodes to only one node at the same time
    // return true if node failed
    public boolean step(int time, boolean canFail) {
        VirtualMachine[] allVms = vms.values().toArray(new VirtualMachine[vms.values().size()]);
        boolean justFailed = false;

        for(VirtualMachine vm : allVms) {
            vm.setRisk(trec.getNodeRisk(this));
            vm.step();
            if(vm.isFinished()) {
                double previousLoad = getLoad();
                remove(vm);
                assert getLoad() < previousLoad;
            }
        }

        if(failure) {
            failureTime++;
            for(VirtualMachine vm : allVms) {
                vm.setStatus(VirtualMachine.Status.FAILURE);
            }
            if(failureTime > RESTART_DURATION) {
                for(VirtualMachine vm : allVms) {
                    // the status of restarting is the same as creating, but it is accounted into the availability calculations
                    vm.setStatus(VirtualMachine.Status.RESTARTING);
                }
                failure = false;
                probabilityOfFailure = 0;
//                System.out.println(nodeId + " got recovered at " + time +": " + vms.size() + " vms");
            }
        } else if(canFail) {
            if(probabilityOfFailure >= 1) {
                failure = true;
                failureTime = 0;
                numberOfFailures++;
                justFailed = true;
//                System.out.println(nodeId + " fails at " + time +": " + vms.size() + " vms");
            } else {
                probabilityOfFailure += 100*(1 + (double)trec.getNodeRisk(this)) / (double)Simulator.MAX_VM_DURATION;
            }
        }
        return justFailed;
    }

    public int getNumberOfFailures() {
        return numberOfFailures;
    }

    public double getProbabilityOfFailure() {
        return probabilityOfFailure;
    }

    public boolean isAboutToFail(double probabilityThreshold) {
        return probabilityOfFailure >= probabilityThreshold;
    }

    public boolean isFitting(VirtualMachine vm) {
        return !failure && vm.getCpus() + usedCPUs <= CPUs;
    }

    public boolean contains(String vmId) {
        return vms.containsKey(vmId);
    }

    public void migrate(String vmId, PhysicalNode destNode) {
        VirtualMachine vm = vms.get(vmId);
        assert vm != null;
        remove(vm);
        destNode.deploy(vm);
        vm.setStatus(VirtualMachine.Status.MIGRATING);
    }

    public double getLoad() {
        assert usedCPUs >= 0 && usedCPUs <= CPUs;
        return (double)usedCPUs/(double)CPUs;
    }

    public int getUsedCPUs() {
        return usedCPUs;
    }

    public boolean isFailure() {
        return failure;
    }
}

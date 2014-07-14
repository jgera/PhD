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
package eu.optimis.hm.simul.ipvm;

import eu.optimis.hm.simul.emotive.VirtualMachine;
import eu.optimis.hm.simul.spvm.Service;
import eu.optimis.hm.simul.tools.MathUtils;
import eu.optimis.hm.simul.trec.TrecApi;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class HolisticManager {

    private FaultToleranceEngine fte;
    private VMManager vmm;
    private Objective objective;
    private TrecApi trec;
    private Set<Service> tasks = new TreeSet<Service>();

    private FileOutputStream vmHistoryFile;
    private FileOutputStream allocationsFile;
    private FileOutputStream availsFile;
    private PrintWriter avails;
    private PrintWriter alloc;
    private PrintWriter vmHistory;
    private String id;

    public enum Objective { RISK_MIN, ECO_MAX }

    public HolisticManager(String id, Objective objective, String filesPath, FaultToleranceEngine.Mode mode) throws FileNotFoundException {
        this.id = id;
        vmHistoryFile = new FileOutputStream(filesPath+"/vmHist_"+id+".csv");
        vmHistory = new PrintWriter(vmHistoryFile);
        availsFile = new FileOutputStream(filesPath+"/avails_"+id+".csv");
        avails = new PrintWriter(availsFile);
        allocationsFile = new FileOutputStream(filesPath+"/allocations_"+id+".csv");
        alloc = new PrintWriter(allocationsFile);

        this.objective = objective;
        this.trec = new TrecApi(this);
        fte = new FaultToleranceEngine(this, mode);
        vmm = new VMManager(this, mode);
    }

    long totalAvailableTime = 0;
    long totalAccountedTime = 0;
    double totalAccountedEco = 0;
    double totalAccountedRisk = 0;
    double measures = 0;
    long numberOfVMs = 0;

    private static final int STEP_SIZE = 1000;
    long stepAvailableTime = 0;
    long stepAccountedTime = 0;

    public String getId() {
        return id;
    }

    public void step(int time, Service serviceToAdd) {
        vmm.step(time);
        fte.step();

        if(serviceToAdd != null) {
            boolean rejectService = false;
            Set<VirtualMachine> accepted = new HashSet<VirtualMachine>();
            for(VirtualMachine vm : serviceToAdd.getVms()) {
                if(vmm.deploy(vm) == null) {
                    rejectService = true;
                } else {
                    accepted.add(vm);
                }
            }

            // a service is only deployed when all the VMs can be deployed
            if(rejectService) {
                for(VirtualMachine vm : accepted) {
                    vmm.undeploy(vm.getId());
                }
            } else {
                tasks.add(serviceToAdd);
            }
        }

        Service[] runningTasks = tasks.toArray(new Service[tasks.size()]);
        int rti = 0;
        while(rti < runningTasks.length && runningTasks[rti].getEndTime() <= time) {
            for(VirtualMachine vm : runningTasks[rti].getVms()) {
                vmm.undeploy(vm.getId());
                vmHistory.println(vm.getId() +
                        "\t" + vm.getCpus() +
                        "\t" + vm.getAvailableTime() + "\t" + vm.getAccountedTime() +
                        "\t" + (double) vm.getAvailableTime() / (double) vm.getAccountedTime() +
                        "\tcreation: " + runningTasks[rti].getStartTime() +
                        "\tend: " + runningTasks[rti].getEndTime() +
                        "\tnow: " + time +
                        "\t" + vm.getStatusHistory());

                numberOfVMs++;
                totalAccountedTime += vm.getAccountedTime();
                totalAvailableTime += vm.getAvailableTime();
                stepAccountedTime += vm.getAccountedTime();
                stepAvailableTime += vm.getAvailableTime();
            }
            tasks.remove(runningTasks[rti]);
            rti++;
        }

        if(time % STEP_SIZE == 0 && stepAccountedTime > 0) {
            avails.println(time + "\t" + (double)stepAvailableTime/(double)stepAccountedTime);
            stepAvailableTime = stepAccountedTime = 0;
        }

        // print stats
        alloc.print(time + "\t" + vmm.getDRP().toString());

        totalAccountedEco += trec.getInfrastructureEco();
        totalAccountedRisk += trec.getInfrastructureRisk();
        measures++;

        alloc.println(trec.getInfrastructureRisk() + "\t" + trec.getInfrastructureEco());
    }

    public void stop() throws IOException {
        alloc.close();
        allocationsFile.close();
        vmHistory.close();
        vmHistoryFile.close();
        avails.close();
        availsFile.close();

        System.out.print(id);
        System.out.println("\t#vms: " + numberOfVMs
                           + "\t%avail: " + (double)totalAvailableTime/(double)totalAccountedTime
                           + "\tavgRisk: " + totalAccountedRisk / measures
                           + "\tavgEco:" + totalAccountedEco / measures
                           + "\tmigrs: " + fte.getMigrations()
                           + "\tfails: " + vmm.getDRP().getNumberOfFailures());

    }

    public VMManager getVMM() {
        return vmm;
    }

    public Objective getObjective() {
        return objective;
    }

    public TrecApi getTrec() {
        return trec;
    }

}

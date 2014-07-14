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

import eu.optimis.hm.simul.emotive.PhysicalNode;
import eu.optimis.hm.simul.emotive.VirtualMachine;
import eu.optimis.hm.simul.trec.TrecApi;

import java.rmi.dgc.VMID;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * In the simulation, it also is accountant of SLA violations
 */
public class FaultToleranceEngine {

    protected HolisticManager hm;

    protected Map<String, Integer> accumulatedNodeRiskLevels = new HashMap<String, Integer>();

    protected Mode mode;


    public FaultToleranceEngine(HolisticManager hm, Mode mode) {
        this.hm = hm;
        this.mode = mode;
    }

    private int migrations = 0;

    public void step() {
        stepReactive();
        if(mode != Mode.REACTIVE) {
            stepProactive();
        }
    }

    public void stepReactive() {
        for(PhysicalNode node : hm.getVMM().getDRP().getAllNodes()) {
            if(node.isFailure()) {
                for(VirtualMachine vm : node.getAllVMs().toArray(new VirtualMachine[node.getAllVMs().size()])) {
                    if(hm.getVMM().deploy(vm) != null) {
                        node.remove(vm);
                        vm.setStatus(VirtualMachine.Status.RESTARTING);
                    }
                }
            }
        }
    }
    public void stepProactive() {
        for(PhysicalNode node : hm.getVMM().getDRP().getAllNodes()) {
            if(node.isAboutToFail(mode.getThreshold()) && !node.isFailure()) {
                // Try to migrate VMs when node about to fail
                for(VirtualMachine vm : node.getAllVMs().toArray(new VirtualMachine[0])) {
                    if(vm.isMigrable()) {
                        PhysicalNode suitable = null;
                        double maxUtility = -1;
                        for(PhysicalNode candidateDest : hm.getVMM().getDRP().getAllNodes()) {
                            if(!candidateDest.isAboutToFail(mode.getThreshold())
                                            && !candidateDest.isFailure()
                                            && candidateDest != node
                                            && candidateDest.isFitting(vm)) {
                                double utility;
                                if(hm.getObjective() == HolisticManager.Objective.ECO_MAX) {
                                    utility = hm.getTrec().getEcoIfDeployment(candidateDest,vm);
                                } else {
                                    utility = TrecApi.MAX_RISK - hm.getTrec().getRiskIfDeployment(candidateDest,vm);
                                }
                                assert utility >= 0;
                                // only can migrate to nodes that are not failed neither about to fail
                                if(utility > maxUtility) {
                                    maxUtility = utility;
                                    suitable = candidateDest;
                                }
                            }
                        }
                        if(suitable != null) {
                            migrations++;
                            node.migrate(vm.getId(), suitable);
                        }
                    }
                }
            }
        }
    }

    public int getMigrations() {
        return migrations;
    }

    public enum Mode {
        REACTIVE,
        PROACTIVE_HIGHRISK {
            @Override
            public double getThreshold() {
                return 0.9;
            }
        },
        PROACTIVE_MEDIUMRISK {
            @Override
            public double getThreshold() {
                return 0.7;
            }
        },
        PROACTIVE_LOWRISK {
            @Override
            public double getThreshold() {
                return 0.5;
            }
        };

        public double getThreshold() {
            return 0;
        }
    };
}

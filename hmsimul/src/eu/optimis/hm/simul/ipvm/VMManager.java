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

import eu.optimis.hm.simul.emotive.DRP;
import eu.optimis.hm.simul.emotive.PhysicalNode;
import eu.optimis.hm.simul.emotive.VirtualMachine;
import eu.optimis.hm.simul.trec.TrecApi;

import java.util.List;

public class VMManager {
    private DRP drp;
    private HolisticManager hm;

    public VMManager(HolisticManager hm, FaultToleranceEngine.Mode mode) {
        drp =  new DRP(hm.getTrec(), mode);
        this.hm = hm;
    }

    public void step(int time) {
        drp.step(time);
    }

    public DRP getDRP() {
        return drp;
    }

    /**
     * Returns the suitable node to deploy, or null if no suitable node
     */
    private PhysicalNode getSuitableNode(VirtualMachine vm) {
        PhysicalNode suitable = null;
        double maxUtility = -1;
        for(PhysicalNode pn : drp.getAllNodes()) {
            if(!pn.isFailure() && pn.isFitting(vm)) {
                double utility;
                if(hm.getObjective() == HolisticManager.Objective.ECO_MAX) {
                    utility = hm.getTrec().getEcoIfDeployment(pn,vm);
                } else {
                    utility = TrecApi.MAX_RISK - hm.getTrec().getRiskIfDeployment(pn,vm);
                }
                assert utility >= 0;
                if(utility > maxUtility) {
                    maxUtility = utility;
                    suitable = pn;
                }
            }
        }
        return suitable;
    }
    public PhysicalNode deploy(VirtualMachine vm) {
        PhysicalNode suitable = getSuitableNode(vm);

        if(suitable != null) {
            suitable.deploy(vm);
        }
        return suitable;
    }

    public void undeploy(String vmId) {
        drp.undeploy(vmId);
    }
}

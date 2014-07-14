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
package eu.optimis.hm.simul.trec;

import eu.optimis.hm.simul.emotive.PhysicalNode;
import eu.optimis.hm.simul.emotive.VirtualMachine;
import eu.optimis.hm.simul.ipvm.HolisticManager;

public class TrecApi {
    protected HolisticManager hm;

    public TrecApi(HolisticManager hm) {
        this.hm = hm;
    }

    // the risk of the infrastructure is the highest risk of its nodes
    public int getInfrastructureRisk() {
        int maxRisk = -1;
        for(PhysicalNode pn : hm.getVMM().getDRP().getAllNodes()) {
            if(!pn.isFailure()) {
                int risk = getNodeRisk(pn);
                if(risk > maxRisk) {
                    maxRisk = risk;
                }
            }
        }
        return maxRisk;
    }

    public int getNodeRisk(PhysicalNode pn) {
        int risk = 1 + (int) Math.ceil(pn.getLoad() * MAX_RISK);
        if(risk > 7) {
            risk = 7;
        }
        assert risk >= 1;
        return risk;
    }

    public int getNodeRisk(String nodeId) {
        PhysicalNode pn = hm.getVMM().getDRP().getNode(nodeId);
        return getNodeRisk(pn);
    }

    public int getRiskIfDeployment(PhysicalNode pn, VirtualMachine vm) {
        int risk = (int) Math.ceil(((double)(vm.getCpus() + pn.getUsedCPUs()) / (double) PhysicalNode.CPUs)*(double)MAX_RISK);
        if(risk > 7) {
            risk = 7;
        }
        assert risk >= 1;
        return risk;

    }

    public double getEcoIfDeployment(PhysicalNode pn, VirtualMachine vm) {
        return ((double)(vm.getCpus() + pn.getUsedCPUs()) / (double) PhysicalNode.CPUs);
    }

    public double getNodeEco(PhysicalNode pn) {
        return(double) pn.getUsedCPUs() / (double)PhysicalNode.CPUs;
    }

    public double getInfrastructureEco() {
        double allNodesEco = 0;
        double numNodes = 0;
        int usedCPUs = 0;
        for(PhysicalNode pn : hm.getVMM().getDRP().getAllNodes()) {
            // nodes with 0 CPUs are suposed to be switched off
            usedCPUs += pn.getUsedCPUs();
            if(!pn.isFailure() && pn.getUsedCPUs() > 0) {
                numNodes++;
                allNodesEco += getNodeEco(pn);
            }
        }
        if(numNodes < 1) {
            return 0.5;
        } else {
            return allNodesEco / numNodes;
        }
    }

    public int getNodeConsumption(PhysicalNode pn) {
        return pn.isFailure() || pn.getUsedCPUs() > 0 ? 1 : 0;
    }

    //public int getInfrastructureCon

    public static final int MAX_RISK = 7;
}

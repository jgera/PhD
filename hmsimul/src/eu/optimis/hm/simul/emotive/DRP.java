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

import eu.optimis.hm.simul.ipvm.FaultToleranceEngine;
import eu.optimis.hm.simul.ipvm.HolisticManager;
import eu.optimis.hm.simul.trec.TrecApi;

import java.util.*;

public class DRP {
    private Map<String, PhysicalNode> nodes = new TreeMap<String, PhysicalNode>();
    FaultToleranceEngine.Mode mode;

    public DRP(TrecApi trec, FaultToleranceEngine.Mode mode) {
        this.mode = mode;
        for(String nodeId : new String[]{"node1", "node2", "node3", "node4"}) {
            nodes.put(nodeId, new PhysicalNode(nodeId,trec));
        }
    }

    public void step(int time) {
        boolean someNodeFailing = false;
        for(PhysicalNode pn : nodes.values()) {
            someNodeFailing = someNodeFailing || pn.isFailure();
        }



        for(PhysicalNode pn : nodes.values()) {
            // a node can fail when other nodes are not failing or about to
            boolean failed = pn.step(time, !someNodeFailing);
            if(failed) {
                for(PhysicalNode otherNode : nodes.values()) {
                    otherNode.setProbabilityOfFailure(0);
                }
            }
        }
    }

    public void deploy(VirtualMachine vm, String destNodeId) {
        nodes.get(destNodeId).deploy(vm);

    }

    public void undeploy(String vmId) {
        boolean found = false;
        for(PhysicalNode node : nodes.values()) {
            if(node.contains(vmId)) {
                node.undeploy(vmId);
                found = true;
            }
        }
        assert found;
    }

    public PhysicalNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public Collection<PhysicalNode> getAllNodes() {
        return nodes.values();
    }

    public VirtualMachine getVirtualMachine(String vmId) {
        for(PhysicalNode pn : nodes.values()) {
            VirtualMachine vm = pn.getVirtualMachine(vmId);
            if(vm != null) {
                return vm;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(PhysicalNode pn : nodes.values()) {
            sb.append(pn.getLoad()).append('\t');
        }
        return sb.toString();
    }

    public int getNumberOfFailures() {
        int failures = 0;
        for(PhysicalNode pn : nodes.values()) {
            failures += pn.getNumberOfFailures();
        }
        return failures;
    }

}

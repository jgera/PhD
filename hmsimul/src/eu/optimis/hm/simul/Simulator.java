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
package eu.optimis.hm.simul;

import eu.optimis.hm.simul.emotive.PhysicalNode;
import eu.optimis.hm.simul.ipvm.FaultToleranceEngine;
import eu.optimis.hm.simul.ipvm.HolisticManager;
import eu.optimis.hm.simul.spvm.Service;
import eu.optimis.hm.simul.tools.MathUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Simulator {

    private static final int MIN_DEPLOY_AVG_FREQUENCY = 15 * 60;
    private static final int MAX_DEPLOY_AVG_FREQUENCY = 60;

    public static final int SIMULATION_DURATION = 24*3600;
    private static final int MIN_VM_DURATION = 5 * 60;
    private static final int VM_DURATION_STDEV = 5 * 60;
    public static final int MAX_VM_DURATION = 30 * 60;

    public static void main(String[] args) throws Exception {

        List<HolisticManager> servers = new LinkedList<HolisticManager>();
        servers.add(new HolisticManager("riskMin_Reactive", HolisticManager.Objective.RISK_MIN, ".", FaultToleranceEngine.Mode.REACTIVE));
        servers.add(new HolisticManager("riskMin_ProactiveLow", HolisticManager.Objective.RISK_MIN, ".", FaultToleranceEngine.Mode.PROACTIVE_LOWRISK));
        servers.add(new HolisticManager("riskMin_ProactiveMed", HolisticManager.Objective.RISK_MIN, ".", FaultToleranceEngine.Mode.PROACTIVE_MEDIUMRISK));
        servers.add(new HolisticManager("riskMin_Proactive", HolisticManager.Objective.RISK_MIN, ".", FaultToleranceEngine.Mode.PROACTIVE_HIGHRISK));
        servers.add(new HolisticManager("ecoMax_Reactive", HolisticManager.Objective.ECO_MAX, ".", FaultToleranceEngine.Mode.REACTIVE));
        servers.add(new HolisticManager("ecoMax_ProactiveLow", HolisticManager.Objective.ECO_MAX, ".", FaultToleranceEngine.Mode.PROACTIVE_LOWRISK));
        servers.add(new HolisticManager("ecoMax_ProactiveMed", HolisticManager.Objective.ECO_MAX, ".", FaultToleranceEngine.Mode.PROACTIVE_MEDIUMRISK));
        servers.add(new HolisticManager("ecoMax_Proactive", HolisticManager.Objective.ECO_MAX, ".", FaultToleranceEngine.Mode.PROACTIVE_HIGHRISK));

        int serviceNum = 0;

        for(HolisticManager server : servers) {
            for(int time = 0 ; time < SIMULATION_DURATION ; time++) {
                int averageDeployFrequency = MIN_DEPLOY_AVG_FREQUENCY - (int) (Math.sin((Math.PI * (double) time)/(double) SIMULATION_DURATION) * (double) (MIN_DEPLOY_AVG_FREQUENCY-MAX_DEPLOY_AVG_FREQUENCY));
                Service service = null;
                if(MathUtils.RND.nextInt(averageDeployFrequency) == 0) {
                    int duration = MathUtils.getNormal(MIN_VM_DURATION, VM_DURATION_STDEV, MAX_VM_DURATION);

                    assert duration >= MIN_VM_DURATION && duration <= MAX_VM_DURATION;
                    service = new Service(server.getId() + "_" + String.valueOf(serviceNum),
                            MathUtils.getNormal(1,3,Service.MAX_VMS_PER_SERVICE),
                            time, time + duration);
                    serviceNum++;
                }
                server.step(time, service);
            }
            for(PhysicalNode pn : server.getVMM().getDRP().getAllNodes() ) {
//                System.out.println(pn.getNodeId() + " pof: " + pn.getProbabilityOfFailure());
            }
            server.stop();
        }
    }

}

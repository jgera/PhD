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

import es.bsc.risk.*;

public class WorkloadUtils {

    private static int slaId = 0;
    // fails if a single node fail
    public static SLA createWebWL(SLA.Policy policy, int numOfSlaves, int cpus, int startTime, int duration) {

        Link[] slaveLinks = new Link[numOfSlaves];

        AbstractNode backend = null;
        if(policy == SLA.Policy.GRAPH_RISK_MIN) {
            backend = new NodeIntersection(new Link[] {
                    new Link(new Node(new Type("Backend1_"+slaId,cpus,startTime,duration))),
                    new Link(new Node(new Type("Backend2_"+slaId,cpus,startTime,duration)))
            });
        } else {
            backend = new Node(new Type("Backend_"+slaId,cpus,startTime,duration));
        }

        for(int i = 0 ; i < numOfSlaves ; i++) {
            slaveLinks[i] = new Link(new Node(new Type("slave"+slaId+"."+i,cpus,startTime,duration),new Link(backend)));
        }
        AbstractNode master = new Node(new Type("master"+slaId,cpus,startTime,duration),new Link(new NodeUnion(slaveLinks)));

        slaId++;

        return new SLA(policy,master, startTime, duration);
    }
}

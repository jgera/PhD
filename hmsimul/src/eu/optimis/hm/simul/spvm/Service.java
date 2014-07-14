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
package eu.optimis.hm.simul.spvm;

import eu.optimis.hm.simul.emotive.VirtualMachine;
import eu.optimis.hm.simul.tools.MathUtils;

public class Service implements Comparable<Service> {
    public static final int MAX_VMS_PER_SERVICE = 8;
    public static final int MAX_CPUS_PER_VM = 4;


    private VirtualMachine[] vms;
    private int endTime;
    private int startTime;

    public Service(String id, int numberOfVMs, int startTime, int endTime) {
        assert numberOfVMs > 0 && numberOfVMs <= MAX_VMS_PER_SERVICE;
        vms = new VirtualMachine[numberOfVMs];
        for(int i = 0 ; i < numberOfVMs ; i++) {
            vms[i] = new VirtualMachine("vm"+id+"-"+i, MathUtils.getNormal(1, 1, MAX_CPUS_PER_VM));
        }
        this.endTime = endTime;
        this.startTime = startTime;
    }

    public VirtualMachine[] getVms() {
        return vms;
    }

    public double getEndTime() {
        return endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    @Override
    public int compareTo(Service o) {
        return endTime - o.endTime;
    }

}

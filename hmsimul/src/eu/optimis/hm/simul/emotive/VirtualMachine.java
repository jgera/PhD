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

import eu.optimis.hm.simul.tools.MathUtils;

import java.util.ArrayList;

public class VirtualMachine {
    private long accountedTime = 0, availableTime = 0, totalSimulationTime = 0;

    private static final int MIN_FAILURE_RATE = 24 * 3600; // 1 each 24 hours

    ArrayList<String>  statusHistory = new ArrayList<String>();

    private String id;
    private int cpus;
    private Status currentStatus = Status.CREATING;

    public VirtualMachine(String id, int cpus) {
        this.cpus = cpus;
        this.id = id;

        statusHistory.add(currentStatus.toString());
    }

    public String getId() {
        return id;
    }

    public int getCpus() {
        return cpus;
    }

    private int failureRate;

    private long lastStatusTime = 0;
    public void step() {
        if(lastStatusTime + currentStatus.getDuration() <= totalSimulationTime) {
            setStatus(currentStatus.nextStatus());
        }
        if(currentStatus.isAccountedIntoSimulation()) {
            accountedTime++;
            if(currentStatus.isAvailable()) {
                availableTime++;
            }
        }
        totalSimulationTime++;

        //simulate random failure
        if(currentStatus != Status.FAILURE && MathUtils.RND.nextInt(failureRate)  == 0) {
            setStatus(Status.FAILURE);
        }
    }

    public void setRisk(int riskLevel) {
        failureRate = MIN_FAILURE_RATE / riskLevel;
    }

    public boolean isFinished() {
        return currentStatus == Status.FINISHED;
    }

    public long getAccountedTime() {
        return accountedTime;
    }

    public long getAvailableTime() {
        return availableTime;
    }

    public long getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public void setStatus(Status status) {
        if( (status  == Status.FAILURE && currentStatus == Status.RUNNING)
            || status != Status.FAILURE) {
            // for simplification, only can fail when status is running
            currentStatus = status;
            lastStatusTime = totalSimulationTime;
            statusHistory.add(currentStatus.toString());
        }
        if(status == Status.FINISHED) {
            assert accountedTime != totalSimulationTime;
        }
    }

    public boolean isMigrable() {
        return currentStatus == Status.RUNNING;

    }

    public enum Status {
        CREATING {
            @Override public long getDuration() { return 120; }
            @Override public Status nextStatus() { return RUNNING; }
            @Override public boolean isAccountedIntoSimulation() { return false; }
        },
        RESTARTING {
            @Override public long getDuration() { return CREATING.getDuration() * 2; }
            @Override public Status nextStatus() { return RUNNING; }
        },
        RUNNING {
            @Override public boolean isAvailable() { return true; }
            @Override public Status nextStatus() { return RUNNING; }
        },
        SHUTDOWN {
            @Override public long getDuration() { return 20; }
            @Override public Status nextStatus() { return FINISHED; }
            @Override public boolean isAccountedIntoSimulation() { return false; }
        },
        FAILURE {
//            @Override public long getDuration() { return 5*60; }
//            @Override public Status nextStatus() { return FAILURE; }
        },
        MIGRATING {
            @Override public long getDuration() { return 30; }
            @Override public Status nextStatus() { return RUNNING; }
        },
        FINISHED {
            @Override public boolean isAccountedIntoSimulation() { return false; }
        };
        public long getDuration() {
            return Long.MAX_VALUE/2; //divided by 2 to avoid overflows
        }

        public boolean isAvailable() {
            return false;
        }
        public boolean isAccountedIntoSimulation() {
            return true;
        }
        public Status nextStatus() {
            return this;
        }
    }

    @Override
    public String toString() {
        return "VirtualMachine{" +
                "accountedTime=" + accountedTime +
                ", availableTime=" + availableTime +
                ", totalSimulationTime=" + totalSimulationTime +
                ", id='" + id + '\'' +
                ", cpus=" + cpus +
                ", currentStatus=" + currentStatus +
                ", lastStatusTime=" + lastStatusTime +
                '}';
    }

    public String getStatusHistory() {
        String hist = "";
        for(String st : statusHistory) {
            hist += st + " ";
        }
        return hist;
    }
}

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
package es.bsc;

import es.bsc.util.Output;
import es.bsc.util.Percentage;
import es.bsc.wl.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

import static es.bsc.Constants.*;



public class Main {

    public static void main(String[] args) throws FileNotFoundException {

        for(int i = 0 ; i < AccountingGroup.DEFAULT_MAX_LIFE_TIME ; i++) {
            Output.POF.println(""+AccountingGroup.getPoFFunction(i));
        }
        //testIntraNodeRiskMin();

    }

    public static void testIntraNodeRiskMin() {

        Provider p = createProvider(Constants.RESOURCES_PER_ACC_GROUP, Constants.CPUS_PER_RESOURCE);
        double[] sumViolations = new double[SLA.Policy.values().length];
        double[] allSLAs = new double[SLA.Policy.values().length];
        for(int i = 0 ; i < SLA.Policy.values().length ; i++) {
            sumViolations[i] = allSLAs[i] = 0;
        }


        double[][] plot = new double[AccountingGroup.DEFAULT_MAX_LIFE_TIME/(7*24)+1][23];
        int week = 0;
        int maxClients = 0, minClients = 0;

        for(int time = 0 ; time < AccountingGroup.DEFAULT_MAX_LIFE_TIME ; time++) {
            if(time == AccountingGroup.DEFAULT_MAX_LIFE_TIME / 2)  {
                addAccountingGroup("Second",time,p, Constants.RESOURCES_PER_ACC_GROUP, Constants.CPUS_PER_RESOURCE);
            }

            double growth = 1+ ((double)time/(double)AccountingGroup.DEFAULT_MAX_LIFE_TIME) * (CLIENTS_MAX_GROWTH - 1);
            int numClients = (int) (growth * (Constants.MIN_CLIENTS_PER_HOUR + (WorkloadGenerator.getWorkloadPercentage(time) *(Constants.MAX_CLIENTS_PER_HOUR- Constants.MIN_CLIENTS_PER_HOUR+1))));

            if(time % (24*7)==0) {
                System.out.print("v0=" + sumViolations[0]);
                System.out.print("\tv1=" + sumViolations[1]);
                System.out.print("\tv2=" + sumViolations[2]);
                System.out.print("\thWL=" + p.getHistoricWorkload());
                System.out.print("\tgrowth=" + growth);
                System.out.print("\tmaxClients=" + maxClients);
                System.out.println("\tminClients=" + minClients);
                maxClients = minClients = numClients;
            } else {
                if(numClients > maxClients) maxClients = numClients;
                if(numClients < minClients) minClients = numClients;
            }
            //Output.tst.println("" + numClients);


            for(int cn = 0 ; cn < numClients ; cn++) {
                SLA slaResponse = null;

                final int cpus = CPUS_AVG + (int) Math.abs(RND.nextGaussian() * CPUS_STDEV);
                final int duration = DURATION_AVG + (int) Math.abs(RND.nextGaussian() * DURATION_STDEV);
                final int slaves = COMPSS_MIN_SLAVES + (time+cn) % (COMPSS_MAX_SLAVES - COMPSS_MIN_SLAVES +1);
                SLA.Policy policy;
                switch((time+cn)%6) {
                    case 0:
                        policy = SLA.Policy.GRAPH_RISK_MIN;
                        break;
                    case 1:
                    case 2:
                        policy = SLA.Policy.NODE_RISK_MIN;
                        break;
                    default:
                        policy = SLA.Policy.COST_MIN;
                }
                SLA slaTemplate = WorkloadUtils.createWebWL(policy, slaves, cpus, time, duration);
                slaResponse = p.checkSLAAllocation(slaTemplate);
                if(slaResponse != null) {
                    allSLAs[policy.ordinal()]++;
                    p.allocateSLA(slaResponse);
                }

            }

            p.step(time);

            plot[week][0]=week;      // time
            plot[week][1]+= p.getActualWorkload(time); // workload %
            double[] rd = p.getAverageRisk(SLA.Policy.COST_MIN);
            plot[week][2] += rd[0];   // risk average for cost minimization slas
            plot[week][3] += rd[1];   // risk stdev for cost minimization slas
            rd = p.getAverageRisk(SLA.Policy.NODE_RISK_MIN);
            plot[week][4] += rd[0];   // risk average for risk minimization slas
            plot[week][5] += rd[1];   // risk stdev for risk minimization slas
            rd = p.getAverageRisk(SLA.Policy.GRAPH_RISK_MIN);
            plot[week][6] += rd[0];   // risk average for risk minimization slas
            plot[week][7] += rd[1];   // risk stdev for risk minimization slas
            rd = p.getAvgAgeofResources(time);
            plot[week][8] += rd[0];   // risk average for normal slas
            plot[week][9] += rd[1];   // risk avg for node risk minimization slas
            plot[week][10] += rd[2];   // risk avg for graph risk minimization slas
            rd = p.getAvgPricePerResUnit();
            plot[week][11] += rd[0];
            plot[week][12] += rd[1];
            plot[week][13] += rd[2];
            rd = p.getAvgRevenuePerResUnit();
            plot[week][14] += rd[0];
            plot[week][15] += rd[1];
            plot[week][16] += rd[2];
            rd = p.getAvgNetBenefitPerResUnit();
            plot[week][17] += rd[0];
            plot[week][18] += rd[1];
            plot[week][19] += rd[2];
            rd = p.getAvgPenaltyPerSLA();
            plot[week][20] += rd[0];
            plot[week][21] += rd[1];
            plot[week][22] += rd[2];

            for(int i = 0 ; i < SLA.Policy.values().length ; i++) {
                sumViolations[i] += rd[i];
            }

            if(time%(7*24) == 7*24-1) {
                for(int i = 0 ; i < plot[week].length ; i++) {
                    plot[week][i] /= 7*24;
                }
                week++;
            }
            Percentage.print(((time * 100) / AccountingGroup.DEFAULT_MAX_LIFE_TIME));

            p.printAllNodesWorkload(time);
        }

        savePlot(plot, "prueba.csv");

        System.out.println("\nviolations: ");
        for(int i = 0 ; i < SLA.Policy.values().length ; i++) {
            System.out.println(SLA.Policy.values()[i].name() + ": " + (sumViolations[i]/(allSLAs[i])));
            System.out.println("sumViolations[i] = " + sumViolations[i]);
            System.out.println("allSLAs = " + allSLAs[i]);
        }


    }

    private static void printFit(Map<String,Type[]> fit) {
        if(fit == null) {
            System.out.println("null");
        } else {
            for(String key : fit.keySet()) {
                System.out.println(key);
                for(Type t : fit.get(key)) {
                    System.out.println("\t"+t.getId());
                }
            }
        }
    }

    private static Provider createProvider(int initialResources, int resourceUnitsPerResource) {
        Provider p = new Provider();
        AccountingGroup a = new AccountingGroup("Initial",0);
        for(int i = 0 ; i < initialResources ; i++) {
            a.addResource(new Resource(p,"res"+i,resourceUnitsPerResource));
        }
        p.addAccountingGroup(a);
        return p;
    }

    private static void addAccountingGroup(String name, int insertionTime, Provider p, int resources, int resourceUnitsPerResource) {
        AccountingGroup a = new AccountingGroup(name,insertionTime);
        for(int j = 0 ; j < resources ; j++) {
            a.addResource(new Resource(p,"res"+(p.getAddedResources() + j),resourceUnitsPerResource));
        }
        p.addAccountingGroup(a);
    }

    private static void savePlot(double[][] plot, String fileName) {
        try {
            PrintWriter ps = new PrintWriter(fileName);
            for(int row = 0 ; row < plot.length ; row++) {
                for(int col = 0 ; col < plot[row].length ; col++) {
                    ps.print(plot[row][col] + "\t");
                }
                ps.println();
            }
            ps.close();
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}

/**
 * Copyright (C) 2010 Universitat Politecnica de Catalunya
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package geneticapp;

import es.bsc.eerm.genetic.Chromosome;
import es.bsc.eerm.genetic.FixedPricer;
import es.bsc.eerm.genetic.GeneticPricer;
import es.bsc.eerm.genetic.Pricer;
import es.bsc.eerm.genetic.RandomPricer;
import es.bsc.eerm.genetic.UtilityBasedPricer;
import java.util.Arrays;
import java.util.Random;

/*
 * Market simulator used for my paper: "A Genetic Model for Pricing in Cloud Computing Markets",
 * published in the 26th Symposium on Applied Computing, special track on Cloud Computing.
 * TaiChung, Taiwan. March 21-25, 2011
 * 
 * <p><a href="http://mario.site.ac.upc.edu/papers/2011SAC.pdf">http://mario.site.ac.upc.edu/papers/2011SAC.pdf</a></p>
 * 
 * @author Mario Macias: mario at upc dot ac dot edu
 */
public class Main {
    public static final int PROVIDER_CAPACITY = 16;
    public static final int SIMULATION_DURATION = (7*5 * 24);

    public static final float TRAINING_DURATION = 7*24;
    public static final float MAX_TASKS_IN_SIMUL = 32;
    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {
        System.out.println("MaxTasks Fixed Utility GeneticRigid Random");
        for(int i = 2 ; i <= MAX_TASKS_IN_SIMUL ; i+= 2) {
            simulation(i);
        }
    }

    public static void simulation(float maxTasks) {

        //FileOutputStream fos = new FileOutputStream("/home/mmacias/Desktop/caca.csv");
        //PrintStream ps = new PrintStream(fos);
        float[] revenues = new float[4];
        Pricer[] providers = new Pricer[4];

        providers[0] = new FixedPricer();
        providers[1] = new UtilityBasedPricer();
        providers[2] = new GeneticPricer(500,0.01f,true);
        //providers[2] = new GeneticPricer(200,0.06f,true);
        providers[3] = new RandomPricer();

        Random rnd = new Random(System.currentTimeMillis());
        float p[] = new float[Chromosome.P_TERMS];
        //max 6 tasks per population
            float proposedBorrame = 0;
            float avgProposedBorrame = 0;
            float avgChoosenBorrame = 0;
            float avgSteps = 0;
        int[] taskAlloc = new int[providers.length];
        float[] avgPrices = new float[providers.length];
        for(float hour = 0 ; hour < SIMULATION_DURATION ; hour+= 1) {
            float intensity = 0.5f + 0.5f * (float)Math.sin((hour / 12f)*Math.PI);
            int ntasks = 2 + (int)((maxTasks / 2f) +  (maxTasks / 2f) * intensity);
            Arrays.fill(taskAlloc, 0);
            Arrays.fill(avgPrices, 0);

            float avgChoosenPrice = 0;
            float successFulPrices = 0;
            float avgRprice = 0;
            for(int idx = 0 ; idx < ntasks ; idx++) {
                p[0] = 1 + (float) Math.floor(rnd.nextFloat() * 3);//qos category
                p[1] = 1 + (float) Math.floor(rnd.nextFloat() * 4);//cpus number
                p[2] = 0.01f + intensity; // aggressivity

                //p[3] = 1 + hour % 24;  //hour of day

                assert p[0] > 0;
                assert p[1] > 0;
                //assert p[2] > 0;
                //assert p[3] > 0;

                float rpBuyer = p[1] * (0.10f + (0.05f * p[0]));
                avgRprice += rpBuyer / p[1];
                float minPrice = Float.MAX_VALUE;
                int choosenIndex = -1;


                int fp = Math.abs(rnd.nextInt());
                int incIdp = Math.abs(rnd.nextInt()) % 2 == 0 ? 1 : -1;
                int idp = 0;
                for(int iters = 0 ; iters < providers.length ; iters++, idp+=incIdp) {
                    int j = (fp + idp) % providers.length;

                    p[2] = 0.01f + (taskAlloc[j] + p[1])  / maxTasks;

                    float pp = providers[j].getPrice(p, rpBuyer);
                    if(j == 2) {
                        proposedBorrame = pp;
                    }

//                    assert pp > 0;
//                    assert pp / p[1] >= Chromosome.getRp(p) / p[1];
                    avgPrices[j] += pp / p[1];
                    if(pp <= rpBuyer && pp < minPrice && taskAlloc[j] + p[1] <= PROVIDER_CAPACITY) {
                        choosenIndex = j;
                        minPrice = pp;
                    }
                    //minPrice = 0.07f * p[1];
                }

                if(choosenIndex >= 0) {
                    avgChoosenBorrame += minPrice / p[1]; // Math.min(minPrice, proposedBorrame) / p[1];
                    avgProposedBorrame += proposedBorrame / p[1];
                    avgSteps++;

                    taskAlloc[choosenIndex] += p[1];
                    avgChoosenPrice += minPrice / p[1];
                    successFulPrices++;
                    if(hour >= TRAINING_DURATION) {
                        revenues[choosenIndex] += minPrice;
                    }

                    if(avgSteps == 1) {
//                        System.out.print(hour + " ");
//                        for(float rev : revenues) {
//                            System.out.print(rev + " ");
//                        }
//                        System.out.println();
                        
                        //float msg = ((avgProposedBorrame - avgChoosenBorrame) / (float)avgSteps);
                        //System.out.println((int)((hour * 100f) / SIMULATION_DURATION) + "% " + msg);
                        //ps.println(msg);
                        avgProposedBorrame = 0;
                        avgChoosenBorrame = 0;
                        avgSteps = 0;
                    }

                }

                for(Pricer pop : providers) {
                    if(pop instanceof GeneticPricer) {
                        ((GeneticPricer)pop).updateGenes((float)Math.min(minPrice,rpBuyer),rpBuyer,p);
                    }
                }
            }
            assert successFulPrices > 0;
            //avgChoosenPrice /= successFulPrices;
//            System.out.print(hour );
//            System.out.print(" " + ntasks);
//            System.out.print(" " + avgChoosenPrice);
//            System.out.print(" " + (avgRprice / ntasks));
//            for(float ap : avgPrices) {
//                System.out.print(" " + (ap / (float)ntasks));
//            }
//            for(float ta : taskAlloc) {
//                System.out.print(" " + (ta / (float)PROVIDER_CAPACITY));
//            }

            //System.out.println();

        }
        System.out.print(maxTasks + " ");
        for(float rev : revenues) {
            System.out.print(rev + " ");
        }
        System.out.println();
        //fos.close();
    }

}

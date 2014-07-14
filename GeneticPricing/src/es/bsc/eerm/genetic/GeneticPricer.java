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

package es.bsc.eerm.genetic;

import java.util.Arrays;
import java.util.Comparator;

/*
 * Market simulator used for my paper: "A Genetic Model for Pricing in Cloud Computing Markets",
 * published in the 26th Symposium on Applied Computing, special track on Cloud Computing.
 * TaiChung, Taiwan. March 21-25, 2011
 *
 * <p><a href="http://mario.site.ac.upc.edu/papers/2011SAC.pdf">http://mario.site.ac.upc.edu/papers/2011SAC.pdf</a></p> 
 * 
 * @author Mario Macias: mario at upc dot ac dot edu
 */
public class GeneticPricer extends Pricer {
    private static final float CROSSINGS_RATE = 0.5f;
    private static final float MEMORY_RATE = 0.9f;

    private int size;

    private float mutationRate;
    private Chromosome[] chromosomes;

    public GeneticPricer(int size, float mutationRate, boolean useDivisor) {
        this.size = size;
        this.mutationRate = mutationRate;
        chromosomes = new Chromosome[size];
        for(int i = 0 ; i < chromosomes.length ; i++) {
            chromosomes[i] = new Chromosome(useDivisor);
        }
    }

    @Override
    public float getPrice(float[] p, float max) {
                for(int i = 1 ; i < chromosomes.length ; i++) {
           chromosomes[i].proposePrice(p,max);
        }
        return chromosomes[0].proposePrice(p, max);

    }

    public int getSize() {
        return size;
    }

    public void orderGenes(float actualPrice, float maxPrice, final float[] p) {
        for(Chromosome c : chromosomes) {
            c.previousScore = c.score;
            c.score = Math.abs(c.lastPrice - actualPrice);

        }

        Comparator<Chromosome> scComparator = new Comparator<Chromosome>() {
            public int compare(Chromosome o1, Chromosome o2) {
                return Double.compare(o2.score,o1.score);
            }
        };
        Arrays.sort(chromosomes, scComparator);
        double sc = 0;
        for(Chromosome c : chromosomes) {
            c.score = sc * (1-MEMORY_RATE) + c.previousScore * MEMORY_RATE;
            sc += 1 / (double) chromosomes.length;
        }
        Arrays.sort(chromosomes, scComparator);
        assert chromosomes[0].score > chromosomes[chromosomes.length - 1].score;

//        final float noisyPrice = actualPrice;// + actualPrice * (rnd.nextFloat() - 0.5f) / 5f;
    }

    public void updateGenes(float actualPrice, float maxPrice, final float[] p) {
        if(size < 10) {return;}
        orderGenes(actualPrice, maxPrice, p);

        // Chooses the chromosomes that must die
        int chromosomeCrossings;
//        final float MAX_DISTANCE = 0.01f * p[1];
//        for(Chromosome c : chromosomes) {
//            float dist = c.lastPrice - actualPrice;
//            if((dist > MAX_DISTANCE)
//                    || (c.lastPrice < c.getRp(p) - MAX_DISTANCE)
//                    || (c.lastPrice > maxPrice + MAX_DISTANCE)) {
//                break;
//            } else {
//                chromosomeCrossings--;
//            }
//        }
//
//        //select genes to be crossed
//        if(chromosomeCrossings > size * 0.8f) {
//            chromosomeCrossings = (int) (size * 0.8f);
//        }
        chromosomeCrossings = (int) (size * CROSSINGS_RATE);
        int nc = chromosomes.length;
        for(int i = 0 ; i < chromosomeCrossings ; i+=2) {
            int ci = (int) Math.abs(rnd.nextInt()) % 2;
            int pi1 = i + ci;
                    //(int)Math.abs(chromosomeCrossings * rnd.nextGaussian()) % chromosomeCrossings; //(int) Math.abs(rnd.nextInt()) % 2;
            int pi2 = i + (ci+1) % 2;
                    //(int)Math.abs(chromosomeCrossings * rnd.nextGaussian()) % chromosomeCrossings; //(int) Math.abs(rnd.nextInt()) % 2;
            Chromosome p1 = chromosomes[pi1];
            Chromosome p2 = chromosomes[pi2];

//            int pi = (int) Math.abs(rnd.nextInt()) % 2;
//            Chromosome p1 = chromosomes[i + pi];
//            Chromosome p2 = chromosomes[i + (pi+1) % 2];

            int crossOverIndex = Math.abs(rnd.nextInt()) % p1.g.length;
            Chromosome[] c = Chromosome.cross(p1, p2, mutationRate, crossOverIndex);
            chromosomes[--nc] = c[0];
            chromosomes[--nc] = c[1];
        }

    }

    @Override
    public String toString() {
        return "g"+chromosomes.length+"m"+mutationRate;
    }





}

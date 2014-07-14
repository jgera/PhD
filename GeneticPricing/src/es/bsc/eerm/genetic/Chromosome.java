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
public class Chromosome {

    public static final int P_TERMS = 3;
    protected float lastPrice;

    protected double score = 0;
    protected double previousScore = 0;

    private static final Random rnd = new Random();

    private static final int REDUNDANT_GENES = 2;
    //genes
    protected float[] g = new float[2 * ((P_TERMS + 1) * P_TERMS) + REDUNDANT_GENES];

    private boolean useDivisor = false;

    public Chromosome(boolean useDivisor) {
        this.useDivisor = useDivisor;
        for(int i = 0 ; i < g.length ; i++) {
            g[i] = (float) rnd.nextGaussian();
        }
    }

    public static Chromosome[] cross(Chromosome parent1, Chromosome parent2, float mutationRate, int crossOverIndex) {
        Chromosome p[] = new Chromosome[] { parent1, parent2 };
        final int c1 = Math.abs(rnd.nextInt()) % 2;
        final int c2 = (c1 + 1) % 2;

        Chromosome c[] = new Chromosome[] { new Chromosome(p[0].useDivisor), new Chromosome(p[0].useDivisor) };

        c[c1].score = p[c1].score * (double)crossOverIndex / (double)c[c1].g.length
                + p[c2].score *(double)(c[c1].g.length - crossOverIndex) / (double)c[c1].g.length;
        c[c2].score = p[c2].score * (double)crossOverIndex / (double)c[c1].g.length
                + p[c1].score *(double)(c[c1].g.length - crossOverIndex) / (double)c[c1].g.length;

        int p1 = c1, p2 = c2;
        for(int i = 0 ; i < p[c1].g.length ; i++) {
            if(i == crossOverIndex) {
                p1 = c2;
                p2 = c1;
            }

            if(rnd.nextFloat() < mutationRate) {
                c[c1].g[i] = p[p1].g[i] * (1f + (float)rnd.nextGaussian());
            } else {
                c[c1].g[i] = p[p1].g[i];// + p[cp].g[i] * rnd.nextFloat();
            }
            if(rnd.nextFloat() < mutationRate) {
                c[c2].g[i] = p[p2].g[i] * (1f + (float)rnd.nextGaussian());
            } else {
                c[c2].g[i] = p[p2].g[i];// + p[cp].g[i] * rnd.nextFloat();
            }
        }

        return c;
    }

    public static float getRp(float p[]) {
        return (p[1] * 0.05f) + p[0] * 0.02f;
    }
    //p[] = pricing terms
    public float proposePrice(float[] p, float max) {
        int cn = 0;

        float rp = getRp(p);

//        float maxUtility = -Float.MAX_VALUE;
//
//        for(float pr = rp ; pr <= max + 0.001 ; pr += (max - rp) / 50f) {
//            float up = (pr - rp) / (max - rp);
//            float util = (float)(0.5 + Math.sin((Math.PI /2) * (2*up+(1-Math.pow(p[2]-1.01f,15))))/2);
//            if(util > maxUtility) {
//                maxUtility = util;
//                lastPrice = pr;
//            }
//        }
//        if(true) return lastPrice;




        //for(int i = 0 ; i < P_TERMS

        lastPrice = 0;
        
        //for(int i = 0 ; i < REDUNDANT_CHROMOSOMES ; i++) {
            lastPrice += g[cn++];
        //}

        float dividend = 0, divisor = 0;
        for(int i = 0 ; i < P_TERMS ; i++) {
            float pterm = g[cn++];
            for(int j = 0 ; j < P_TERMS ; j++) {
                pterm *= Math.pow(p[j],g[cn++]);
            }
            dividend += pterm;
        }

        if(!useDivisor) {
            divisor = 1;
        } else {
            for(int i = 0 ; i < P_TERMS ; i++) {
                float pterm = g[cn++];
                for(int j = 0 ; j < P_TERMS ; j++) {
                    pterm *= Math.pow(p[j],g[cn++]);
                }
                divisor += pterm;
            }
        }
        if(divisor == 0) {
            lastPrice = 1e10f;
        } else {
            lastPrice += dividend / divisor + g[cn++];
        }

        if(Float.isNaN(lastPrice) || Float.isInfinite(lastPrice)) {
            lastPrice = 1e10f;
        }

        lastPrice = Math.abs(lastPrice) + rp;

//        //price musn't be lesser than reservation price
//        
//        if(lastPrice < rp) {
//            return rp;
//        } else if(lastPrice > max) {
//            return max;
//        }
        return lastPrice;
    }



}

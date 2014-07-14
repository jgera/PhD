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

/*
 * Market simulator used for my paper: "A Genetic Model for Pricing in Cloud Computing Markets",
 * published in the 26th Symposium on Applied Computing, special track on Cloud Computing.
 * TaiChung, Taiwan. March 21-25, 2011
 * 
 * <p><a href="http://mario.site.ac.upc.edu/papers/2011SAC.pdf">http://mario.site.ac.upc.edu/papers/2011SAC.pdf</a></p>
 * 
 * @author Mario Macias: mario at upc dot ac dot edu
 */
public class UtilityBasedPricer extends Pricer {

    public UtilityBasedPricer() {
    }

    @Override
    public float getPrice(float[] p, float max) {
        float agg = p[2] - 0.01f;
        float rp = Chromosome.getRp(p);
        float bestPrice = rp, maxUtility = -Float.MAX_VALUE;
        for(float pr = rp ; pr <= max + 0.001 ; pr += (max - rp) / 50f) {
            float up = (pr - rp) / (max - rp);
            float util = (float)(0.5 + Math.sin((Math.PI /2) * (2*up+(1-Math.pow(agg,15))))/2);
            if(util > maxUtility) {
                maxUtility = util;
                bestPrice = pr;
            }
        }
        return bestPrice;

    }

    @Override
    public String toString() {
        return "Utility";
    }





}

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
public class FixedPricer extends Pricer {

    public FixedPricer() {
    }

    @Override
    public float getPrice(float[] p, float max) {
        float rp = Chromosome.getRp(p);
        return rp + (max - rp) * 0.04f;
    }

    @Override
    public String toString() {
        return "Fixed";
    }


}

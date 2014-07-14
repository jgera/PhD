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
package es.bsc.util;

import java.util.ArrayList;
import java.util.Vector;

public class Stat {
    private ArrayList<Double> values = new ArrayList<Double>();
    private double numberOfValues = 0;
    private double sum = 0;


    public void addValue(double value) {
        values.add(value);
        numberOfValues++;
        sum += value;
    }
    /**
     *
     * @return index 0 is the mean, index 1 is the standard deviation
     */
    public double[] getStats() {
        double[] stats = new double[2];
        stats[0] = sum/numberOfValues;
        stats[1] = 0;
        for(Double d : values) {
            double n = (d-stats[0]);
            stats[1] += n*n;
        }

        stats[1] = Math.sqrt(stats[1]/numberOfValues);
        return stats;
    }
}

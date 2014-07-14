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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class is a set of all resources that have the same age and are amortized in group
 */
public class AccountingGroup {
    private String id;
    private Map<String, Resource> resources = new TreeMap<String, Resource>();
    protected int insertionTime = 0;
    protected int maxLifeTime = DEFAULT_MAX_LIFE_TIME;
    protected int currentTime;

    protected double TCO = 0;
    protected double totalAmortized = 0;
    protected double totalResourceUnits = 0;

    public AccountingGroup(String id, int insertionTime) {
        this.id = id;
        this.insertionTime = insertionTime;
    }

    public AccountingGroup(String id, int insertionTime, int maxLifeTime) {
        this.id = id;
        this.insertionTime = insertionTime;
        this.maxLifeTime = maxLifeTime;
    }

    public void addResource(Resource r) {
        r.accountingGroup = this;
        TCO += r.TCO;
        totalResourceUnits += r.maxResourceUnits;
        resources.put(r.getId(),r);
    }

    public Resource getResource(String id) {
        return resources.get(id);
    }

    public Iterator<Resource> getResourcesIterator() {
        return resources.values().iterator();
    }

    public String getId() {
        return id;
    }

    public double getAmortizationPercentage() {
        if(totalAmortized > TCO) {
            return 1;
        } else {
            return totalAmortized/ TCO;
        }
    }

    public double getTotalResourceUnits() {
        return totalResourceUnits;
    }

    /* function that describes the PoF, being high in the first months and the last monts of lifetime
             */
    // todo: make pof function slightly increase during minimum pof time
    public static double getPoFFunction(int currentLifeTime) {
        if(currentLifeTime < NORMAL_INIT) {
            return (1-Math.cos(((currentLifeTime-NORMAL_INIT)/(NORMAL_INIT*2))*Math.PI))*(MAX_POF_INIT-MIN_POF_START)+MIN_POF_START;
        } else if(currentLifeTime >= NORMAL_END) {
            return (1-(Math.cos((currentLifeTime-NORMAL_END)/(2*(DEFAULT_MAX_LIFE_TIME-NORMAL_END))*Math.PI)))*(MAX_POF_END-MIN_POF_END)+MIN_POF_END;
        } else {
            double x1,x2,y1,y2;
            x1 = NORMAL_INIT;
            x2 = NORMAL_END;
            y1 = MIN_POF_START;
            y2 = MIN_POF_END;
            return ((y2-y1)/(x2-x1)) * (currentLifeTime - x1) + y1;
        }
    }
    // constants for the PoFFunction
    // 36 months of life time (in hours)
    // we use years of 360 days (Actual/360 date format)
    public static final int DEFAULT_MAX_LIFE_TIME = 36 * 30 * 24;
    protected static final double MAX_POF_INIT = 0.03;
    protected static final double MIN_POF_START = 0.005;
    protected static final double MIN_POF_END = 0.01;
    protected static final double MAX_POF_END = 0.06;
    protected static final double NORMAL_INIT = 2.5 * 30 * 24;
    protected static final double NORMAL_END = DEFAULT_MAX_LIFE_TIME - 3.5 * 30 * 24;


}

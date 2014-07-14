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

import java.util.Random;

public class Constants {
    public static final Random RND = new Random(System.currentTimeMillis());
    public static final int RESOURCES_PER_ACC_GROUP = 50;
    public static final int CPUS_PER_RESOURCE = 16;
    public static final double MIN_CLIENTS_PER_HOUR = 5;
    public static final double MAX_CLIENTS_PER_HOUR = 60;
    public static final int CPUS_AVG = 1;
    public static final int CPUS_STDEV = 1;

    public static final int DURATION_AVG = 1;
    public static final int DURATION_STDEV = 1;

    public static final int COMPSS_MIN_SLAVES = 2;
    public static final int COMPSS_MAX_SLAVES = 4;

    public static final double RESOURCE_RISK_WEIGHT = 0.02;
    public static final double WORKLOAD_RISK_WEIGHT = 0.02;

    public static final double DEFAULT_TCO = 4000;

    public static final double CLIENT_RP_COMPUTE_UNIT_HOUR = 0.15;

    public static final double CLIENTS_MAX_GROWTH = 2.0;

    // same values as our previous work
    //GoldQoS tasks have a Reservation Price for the seller 50% higher
    //than the Bronze Reservation Price, and Silver tasks have a
    //Reservation Price 20%
    //MRT(Bronze; Silver; Gold) = (15%;5%;3%),
    //MP T(Bronze; Silver; Gold) = (75%;50%;30%) and
    //MP(Bronze; Silver; Gold) = (MR;2MR;3MR)
    public static final double[] OVERPRICE = {1, 1.5, 2};
    public static final double[] MRT = {0.15, 0.10, 0.05};
    public static final double[] MPT = {0.75, 0.5, 0.3};
    public static final double[] MP = {-1,-1.5,-2};

}

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * @author mmacias
 */
public class WorkloadGenerator {
    private static long WL_GRANULARITY_MS = 60 * 60000;//60-minutes granularity
    private static long WORKLOAD_LENGTH_MS = 7 * 24 * 60 * 60 * 1000;//1 week length

    private static double[] basePercentage = null;

    static {
        basePercentage = new double[(int) (WORKLOAD_LENGTH_MS / WL_GRANULARITY_MS)];
        try {
            load("/residencia.txt");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private double tmpWl = 0;

    public static double getWorkloadPercentage(long currentHour) {
        return basePercentage[(int) (currentHour % basePercentage.length)];
    }

    private static void load(String workloadFile) throws IOException, ParseException {
        DateFormat sdf = new SimpleDateFormat("d HH:mm:ss");
        int currentStep = 0;
        Date lastTimeGrain = null;
        double max = 0;

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(WorkloadGenerator.class.getResourceAsStream(workloadFile)));

        String line = reader.readLine();
        double tracesHour = 0;
        double currentWorkload = 0;

        while (line != null && currentStep < basePercentage.length) {

            StringTokenizer tokens = new StringTokenizer(line);

            tokens.nextToken();
            tracesHour++;
            currentWorkload += Long.parseLong(tokens.nextToken());

            for (int i = 0; i < 3; i++) {
                tokens.nextToken();
            }


            Date currentTimeGrain = sdf.parse(tokens.nextToken() + " " + tokens.nextToken());
            if (lastTimeGrain == null) {
                lastTimeGrain = currentTimeGrain;
            }
            if (currentTimeGrain.getTime() - lastTimeGrain.getTime() >= WL_GRANULARITY_MS) {
                currentWorkload /= tracesHour;
                basePercentage[currentStep++] = currentWorkload;
                if (currentWorkload > max) {
                    max = currentWorkload;
                }
                currentWorkload = 0;
                tracesHour = 0;
                lastTimeGrain = currentTimeGrain;
            }

            line = reader.readLine();
        }
        for (int i = 0; i < basePercentage.length; i++) {
            basePercentage[i] /= max;
        }
    }
}

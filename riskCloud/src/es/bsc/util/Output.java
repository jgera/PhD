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

import java.io.PrintStream;

public enum Output {
    POF, NODESWL, tst; //.... change/add more output files

    private PrintStream out = null;
    Output() {
        try {
            out = new PrintStream(this.name().toLowerCase() +".csv");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for(Output o : Output.values()) {
                    o.out.close();
                }
            }
        }));
    }

    public void println(String x) {
        out.println(x);
    }



}

package edu.upc.mario.trustsim.util;

import java.io.File;
import java.io.PrintStream;

public enum Output {
    PROVIDERS, EARNINGS, REPUTATIONS;

    private PrintStream out = null;
    Output() {

    }

    private void initializeOutput() {
        initializeOutput(null);
    }
    private void initializeOutput(String path) {
        if(path == null) {
            path = ".";
        }
        try {
            if(out != null) {
                out.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            out = new PrintStream(path+ File.separator+this.name().toLowerCase() +".csv");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                for(Output o : Output.values()) {
                    if(o.out != null) {
                        o.out.close();
                    }
                }
            }
        }));
    }

    public void println(String x) {
        if(out == null) {
            initializeOutput();
        }
        out.println(x);
    }



}

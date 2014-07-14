package edu.upc.mario.trustsim;

public class Const {
    public static final int     STEPS_PER_CYCLE     = 24*60;
    public static final int     SIMULATION_STEPS    = STEPS_PER_CYCLE * 3;
    public static final double  TASK_MAX_CPUs       = 4;
    public static final int     TASK_MIN_DURATION   = 10;
    public static final int     TASK_MAX_DURATION   = 50;

    public static final int     NODE_MAX_CPUs       = 32;

    public static final double UNKNOWN_REPUTATION = 0.3;

    public static final double TRUST_RECOVERY_RATE = 0.1;

    public static final double MAX_DEPLOYMENT_FREQUENCY = 1;
    public static final double MIN_DEPLOYMENT_FREQUENCY = 3;

    public static final int REVENUE_WINDOW_LENGTH = 120;
    public static final int FULFILLMENT_WINDOW_LENGTH = 30;

    public static final double MAX_FULFILLMENT_TO_SWITCH = 0.2;

    public static final double PRICE_BONUS_FOR_REPUTATION = 0.1;

    public static final int NUMBER_OF_CLIENTS = 15;

}

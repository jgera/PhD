package edu.upc.mario.trustsim.tasks;

import edu.upc.mario.trustsim.Provider;
import edu.upc.mario.trustsim.wl.Node;
import edu.upc.mario.trustsim.wl.Task;

import java.util.Set;

public abstract class TaskCanceler {
    protected Provider provider;

    public TaskCanceler(Provider provider) {
        this.provider = provider;
    }

    public abstract Set<Task> selectSLAsToViolate(Node n, double load, long time);
}

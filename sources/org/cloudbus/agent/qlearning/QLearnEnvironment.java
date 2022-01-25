package org.cloudbus.agent.qlearning;

public interface QLearnEnvironment {
    double getReward();
    int getState();
    int getNumStates();
}

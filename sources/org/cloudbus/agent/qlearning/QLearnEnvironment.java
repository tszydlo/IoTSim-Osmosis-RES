package org.cloudbus.agent.qlearning;

public interface QLearnEnvironment {
    void update(int actionId);
    double getReward(QLearnAgent agent);
    int getState();

    int updateAndGetNewState(QLearnAgent agent, int actionId);
}

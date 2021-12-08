package org.cloudbus.agent.qlearning;

public interface QLearnEnvironment {

    double getReward(QLearnAgent agent);

    int updateAndGetNewState(QLearnAgent agent, int actionId);
}

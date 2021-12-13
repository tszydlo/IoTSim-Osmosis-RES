package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.qlearning.QLearnAgent;
import org.cloudbus.agent.qlearning.QLearnEnvironment;

public class RES_RL_example1_Environment implements QLearnEnvironment {
    double sensingRate;
    double batteryLevel;
    double dataAge;

    @Override
    public void update(int actionId) {

    }

    @Override
    public double getReward(QLearnAgent agent) {
        //alpha * r_q + (1-alpha) * r_e
        return 0;
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public int updateAndGetNewState(QLearnAgent agent, int actionId) {
        return 0;
    }
}

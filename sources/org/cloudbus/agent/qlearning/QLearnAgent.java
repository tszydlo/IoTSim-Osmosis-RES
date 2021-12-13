package org.cloudbus.agent.qlearning;

import com.github.chen0040.rl.learning.qlearn.QAgent;
//import jdk.internal.org.jline.utils.Log;
import org.cloudbus.agent.AbstractAgent;

public class QLearnAgent extends AbstractAgent {

    private QAgent qAgent;
    private QLearnEnvironment environment;

    private final String name = "SomeName";

    private static final int DEFAULT_NUMBER_OF_STATES = 100;
    private static final int DEFAULT_NUMBER_OF_ACTIONS = 10;

    public QLearnAgent(QLearnEnvironment environment) {
        this(DEFAULT_NUMBER_OF_STATES, DEFAULT_NUMBER_OF_ACTIONS, environment);
    }

    public QLearnAgent(int numberOfStates, int numberOfActions, QLearnEnvironment environment) {
        this.qAgent = new QAgent(numberOfStates, numberOfActions);
        this.environment = environment;
    }

    private int previousActionId;
    private double previousReward;
    private int currentState;

    @Override
    public void monitor() {
        this.previousReward = environment.getReward(this);
        //Log.debug(String.format("Agent %s gets reward: %f for action %d.",
        //        this.name, this.previousReward, this.previousActionId));
    }

    @Override
    public void analyze() {
        this.qAgent.update(this.previousActionId, this.currentState, this.previousReward);
    }

    @Override
    public void plan() {
        this.previousActionId =  qAgent.selectAction().getIndex();
        //Log.debug(String.format("Agent %s selects action: %d.", this.name, this.previousActionId));
    }

    @Override
    public void execute() {
        // Is it possible to know new state instantly after updating environment?
        this.currentState = environment.updateAndGetNewState(this, this.previousActionId);
        //Log.debug(String.format("Agent %s gets new state: %d for action %d.",
        //        this.name, this.currentState, this.previousActionId));
    }
}

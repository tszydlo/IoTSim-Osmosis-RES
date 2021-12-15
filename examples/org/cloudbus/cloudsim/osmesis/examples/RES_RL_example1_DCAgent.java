package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.AgentMessage;
import org.cloudbus.agent.DCAgent;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.List;

public class RES_RL_example1_DCAgent extends DCAgent {

    List<AgentMessage> messages;

    RES_RL_example1_Environment environment;

    public RES_RL_example1_DCAgent() {
        environment = new RES_RL_example1_Environment();
    }

    @Override
    public void monitor() {
        super.monitor();

        System.out.println( (int)(CloudSim.clock()%(24*3600)/3600)  );

    }

    @Override
    public void analyze() {
        super.analyze();
    }

    @Override
    public void plan() {
        super.plan();

        messages = getReceivedMessages();

        //choose the MEL instance from Edge with highest RES value
        for(AgentMessage message:messages) {
            RES_RL_example1_AgentMessage res_rl_message = (RES_RL_example1_AgentMessage) message;

            if (res_rl_message.isToEdge()){
                //update environment
                //TODO
                System.out.println("Message from device "+res_rl_message.getSOURCE()+" Battery Level = "+res_rl_message.getBatteryLevel());

                environment.setBatteryLevelCtx(res_rl_message.getBatteryLevel());
                //environment.setChargingCtx(res);
                environment.setSensingRateCtx(res_rl_message.getSensingRate());
                //environment.setForecastCtx();
            }
        }
    }

    @Override
    public void execute() {
        super.execute();

        //Create new message.
        RES_RL_example1_AgentMessage res_rl_message = (RES_RL_example1_AgentMessage) newAgentMessage();

        res_rl_message.setSensingRate(123);
        res_rl_message.setToDevice();
        //Send to all neighbours (null destination means all - follows the agent topology defined in the example file).
        res_rl_message.setDESTINATION(null);
        publishMessage(res_rl_message);
    }
}

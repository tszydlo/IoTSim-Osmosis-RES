package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.AgentMessage;
import org.cloudbus.agent.DeviceAgent;

import java.util.List;

public class RES_RL_example1_DeviceAgent extends DeviceAgent {
    List<AgentMessage> messages;

    @Override
    public void monitor() {
        super.monitor();
    }

    @Override
    public void analyze() {
        super.analyze();

        //Create new message.
        RES_RL_example1_AgentMessage res_rl_message = (RES_RL_example1_AgentMessage) newAgentMessage();

        res_rl_message.setBatteryLevel(this.getIoTDevice().getBattery().getCurrentCapacity());
        //res_rl_message.setSensingRate(this.getIoTDevice().);
        res_rl_message.setToEdge();

        //Send to all neighbours (null destination means all - follows the agent topology defined in the example file).
        res_rl_message.setDESTINATION(null);
        publishMessage(res_rl_message);
    }

    @Override
    public void plan() {
        super.plan();

        messages = getReceivedMessages();

        //choose the MEL instance from Edge with highest RES value
        for(AgentMessage message:messages) {
            RES_RL_example1_AgentMessage res_rl_message = (RES_RL_example1_AgentMessage) message;

            if (res_rl_message.isToDevice()){
                System.out.println("Message from device "+res_rl_message.getSOURCE()+" Device="+this.getName() + " New sensing rate="+res_rl_message.getSensingRate());
            }
        }
    }

    @Override
    public void execute() {
        super.execute();
    }
}

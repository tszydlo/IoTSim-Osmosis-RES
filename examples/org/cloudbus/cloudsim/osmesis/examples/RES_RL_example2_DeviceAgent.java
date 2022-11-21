package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.DeviceAgent;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;
import java.util.stream.Collectors;

public class RES_RL_example2_DeviceAgent extends DeviceAgent {
    List<RES_RL_example2_AgentMessage> messages;

    private int getSensorId() {
        String name = this.getIoTDevice().getName();
        return Integer.parseInt(name.substring(name.length() - 1));
    }

    private List<RES_RL_example2_AgentMessage> getCastMessages() {
        return getReceivedMessages().stream()
                .map(RES_RL_example2_AgentMessage.class::cast)
                .filter(msg -> getSensorId() == msg.getDeviceId())
                .collect(Collectors.toList());
    }

    @Override
    public void monitor() {
        super.monitor();
    }

    @Override
    public void analyze() {
        super.analyze();

        //Create new message.

        messages = this.getCastMessages();

        double sensingRate = messages.get(0).sensingRate;
        this.getIoTDevice().setUpdateIoTDeviceDataRate(300);
        this.getIoTDevice().getBattery().setBatterySensingRate(sensingRate);

        RES_RL_example2_AgentMessage res_rl_message = (RES_RL_example2_AgentMessage) newAgentMessage();

        res_rl_message.setBatteryLevel(this.getIoTDevice().getBattery().getCurrentCapacity() / getIoTDevice().getBattery().getMaxCapacity());

        res_rl_message.setToEdge();

        res_rl_message.deviceId = getSensorId();

        //Send to all neighbours (null destination means all - follows the agent topology defined in the example file).
        res_rl_message.setDESTINATION(null);

        publishMessage(res_rl_message);
    }

    @Override
    public void plan() {
        super.plan();
    }

    @Override
    public void execute() {
        super.execute();
    }
}

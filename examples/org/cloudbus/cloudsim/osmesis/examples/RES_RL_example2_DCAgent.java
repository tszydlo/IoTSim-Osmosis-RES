package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.DCAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class RES_RL_example2_DCAgent extends DCAgent {

    List<RES_RL_example2_AgentMessage> messages;

    double sensing_sum = 0;
    double sensing_iter = 0;

    Random random;

    public RES_RL_example2_DCAgent() {
        random = new Random();
    }

    @Override
    public void monitor() {
        super.monitor();
    }

    @Override
    public void analyze() {
        super.analyze();
        if(!getName().startsWith("Edge")){
            return;
        }
        boolean await = true;
        boolean logged = false;
        while (await) {
            if (!logged) {
                System.out.println("Awaiting for mqtt message. my name is " + getName());
                logged = true;
            }
            if (!RL_BROKER.awaitingMessage) {
                for (int i = 0; i < 4; i++) {
                    RES_RL_example2_AgentMessage msg = (RES_RL_example2_AgentMessage) newAgentMessage();
                    msg.setToDevice(i);
                    msg.setSensingRate(RL_BROKER.getSensingRates(Integer.toString(i)));
                    publishMessage(msg);
                    RL_BROKER.awaitingMessage = true;
                }
                await = false;
            } else {
                try {
                    Thread.sleep(125);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void plan() {
        super.plan();
        if(!getName().startsWith("Edge")){
            return;
        }

        messages = getReceivedMessages().stream()
                .map(RES_RL_example2_AgentMessage.class::cast)
                .collect(Collectors.toList());

        Map<Integer, Double> energyMap = new HashMap<>();

        for (RES_RL_example2_AgentMessage message : messages) {

            if (message.isToEdge()) {
                energyMap.put(message.deviceId, message.getBatteryLevel());

            }
        }

        RL_BROKER.publishNewEnergyMessage(energyMap);
        System.out.println("Published message to mqtt:");
        System.out.println(energyMap);
    }

    @Override
    public void execute() {
        super.execute();
    }
}

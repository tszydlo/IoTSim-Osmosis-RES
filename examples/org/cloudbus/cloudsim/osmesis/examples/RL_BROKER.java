package org.cloudbus.cloudsim.osmesis.examples;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Map;

public class RL_BROKER {
    private static Map<String, Long> lastMessage;
    private static double K = 0.35;
    public static IMqttClient mqttClient;
    public static String mqttTopicRange = "senscoveragerange";
    public static String mqttTopicEnergy = "senscoverageenergy";

    private static JSONParser parser = new JSONParser();

    public synchronized static void newSensingRangeMessage(Map<String, Long> message) {
        lastMessage = message;
        awaitingMessage = false;
    }

    public synchronized static void publishNewEnergyMessage(Map<Integer, Double> message) {
        MqttMessage msg = new MqttMessage();
        msg.setPayload(new JSONObject(message).toJSONString().getBytes());
        try {
            mqttClient.publish(mqttTopicEnergy, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public static boolean awaitingMessage = true;


    public synchronized static double getSensingRates(String deviceId) {
        long sensingRange = lastMessage.getOrDefault(deviceId, 0L);
        if (sensingRange == 0) {
            return 0.001;
        } else {
            return sensingRange * K;
        }
    }
}

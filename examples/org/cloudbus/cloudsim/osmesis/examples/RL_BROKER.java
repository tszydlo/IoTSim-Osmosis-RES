package org.cloudbus.cloudsim.osmesis.examples;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Map;

public class RL_BROKER {
    private static Map<Integer, Integer> lastMessage;
    private static double K = 1.0;
    private static double MAX_SENSING_RATE = 300.0;
    public static IMqttClient mqttClient;
    public static String mqttTopicRange = "senscoveragerange";
    public static String mqttTopicEnergy = "senscoverageenergy";

    private static JSONParser parser = new JSONParser();

    public synchronized static void newSensingRangeMessage(Map<Integer, Integer> message) {
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


    public synchronized static double getSensingRates(int deviceId) {
        double sensingRange = lastMessage.getOrDefault(deviceId, 0) * 1.0;
        if (sensingRange == 0) {
            return MAX_SENSING_RATE;
        }
        return 1 / (sensingRange * sensingRange * Math.PI * K);
    }
}

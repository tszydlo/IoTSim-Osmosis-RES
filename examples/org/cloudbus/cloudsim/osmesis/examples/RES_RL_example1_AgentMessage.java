package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.AgentMessage;

public class RES_RL_example1_AgentMessage extends AgentMessage {
    boolean toEdge = false;
    boolean toDevice = false;

    double sensingRate;

    double batteryLevel;
    double lastTransmit;

    public void setToEdge() {
        this.toEdge = true;
        this.toDevice = false;
    }

    public void setToDevice() {
        this.toDevice = true;
        this.toEdge = true;
    }

    public boolean isToDevice() {
        return toDevice;
    }

    public boolean isToEdge() {
        return toEdge;
    }

    public double getSensingRate() {
        return sensingRate;
    }

    public void setSensingRate(double sensingRate) {
        this.sensingRate = sensingRate;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public double getLastTransmit() {
        return lastTransmit;
    }

    public void setLastTransmit(double lastTransmit) {
        this.lastTransmit = lastTransmit;
    }
}

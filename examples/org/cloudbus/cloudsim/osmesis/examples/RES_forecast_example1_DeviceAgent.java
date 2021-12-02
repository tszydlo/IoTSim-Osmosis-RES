package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.DeviceAgent;

public class RES_forecast_example1_DeviceAgent extends DeviceAgent {

    public RES_forecast_example1_DeviceAgent() {
        //This is necessary for dynamic agent instance creation.
    }

    @Override
    public void monitor() {
        super.monitor();

        double capacity = getIoTDevice().getBattery().getCurrentCapacity();

        if (getIoTDevice().getBattery().isResPowered()){
            System.out.println("["+getIoTDevice().getName() + "]Battery capacity:"+(int) capacity + "mAh\t Charging:" + getIoTDevice().getBattery().isCharging() +"\t Charging current:"+(int)getIoTDevice().getBattery().getChargingCurrent()+"mA");
            //System.out.println((int) capacity);
            //System.out.println((int) getIoTDevice().getBattery().get);
        }

    }

    @Override
    public void analyze() {
        super.analyze();

        //Do nothing.
    }

    @Override
    public void plan() {
        super.plan();

        //Do nothing.
    }

    @Override
    public void execute() {
        super.execute();

        //Do nothing.
    }
}

package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.DCAgent;
import org.cloudbus.res.dataproviders.ForecastData;

public class RES_forecast_example1_DCAgent extends DCAgent {

    ForecastData forecast;

    public RES_forecast_example1_DCAgent() {
        //This is necessary for dynamic agent instance creation.
    }

    @Override
    public void monitor() {
        super.monitor();

        if (energyController.getEnergySources().get(0).getEnergyData().isForecast()){
            forecast = (ForecastData) energyController.getEnergySources().get(0).getEnergyData();
        };

        //forecast.getNDayForecast();

    }

    @Override
    public void analyze() {
        super.analyze();

    }

    @Override
    public void plan() {
        super.plan();

        //There should not be any message in input queue.

        //Do nothing in DC.
    }

    @Override
    public void execute() {
        super.execute();

        //Do nothing in DC.
    }
}

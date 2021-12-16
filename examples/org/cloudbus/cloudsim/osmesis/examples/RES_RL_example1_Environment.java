package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.qlearning.QLearnAgent;
import org.cloudbus.agent.qlearning.QLearnEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RES_RL_example1_Environment implements QLearnEnvironment {
    static String BATTERY_LEVEL="battery_level";
    static String SENSING_RATE="sensing_rate";
    static String NEXTDAY_FORECAST="nextday_forecast";
    static String TODAY_FORECAST="today_forecast";
    static String TIME="time";

    double sensingRateCtx;
    double batteryLevelCtx;
    double nextdayForecastCtx;
    double todayForecastCtx;
    int timeCtx;

    Map<String, Integer> maxCtxElements;
    Map<String, Integer> ctx2index;
    List<String> ctxElements;

    List<Integer> context;

    public RES_RL_example1_Environment() {
        maxCtxElements = new HashMap<>();
        ctx2index = new HashMap<>();
        ctxElements = new ArrayList<>();
        context = new ArrayList<>();

        add_context_element(BATTERY_LEVEL, 5);
        add_context_element(SENSING_RATE, 5);
        add_context_element(NEXTDAY_FORECAST, 3);
        add_context_element(TODAY_FORECAST, 3);
        add_context_element(TIME, 4);
    }

    private void add_context_element(String name, int max_num_of_elements){
        ctxElements.add(name);
        ctx2index.put(name,ctxElements.size()-1);
        maxCtxElements.put(name, max_num_of_elements);
        context.add(0);
    }

    @Override
    public void update(int actionId) {

    }

    public double getReward() {
        //alpha * r_q + (1-alpha) * r_e

        if (batteryLevelCtx < 0.05){
            return -1;
        } else {
            return 100.0/sensingRateCtx;
            //return batteryLevelCtx;
        }
    }

    @Override
    public double getReward(QLearnAgent agent) {
        //alpha * r_q + (1-alpha) * r_e
        return 0;
    }

    @Override
    public int getState() {
        int state = 0;
        int mult = 1;
        for(int i =0; i< context.size(); i++){
            state+=context.get(i) * mult;
            mult*=context.get(i);
        }
        return state;
    }

    public int getNumStates() {
        int states = 1;
        for(int i =0; i< context.size(); i++){
            states*=context.get(i);
        }
        return states;
    }


    @Override
    public int updateAndGetNewState(QLearnAgent agent, int actionId) {
        return 0;
    }

    public void setSensingRateCtx(double sensingRateCtx) {
        this.sensingRateCtx = sensingRateCtx;
        if (sensingRateCtx < 1.0) {
            context.set(ctx2index.get(SENSING_RATE),0);
            return;
        };

        if (sensingRateCtx < 2.0) {
            context.set(ctx2index.get(SENSING_RATE),1);
            return;
        };

        if (sensingRateCtx < 3.0) {
            context.set(ctx2index.get(SENSING_RATE),2);
            return;
        };

        if (sensingRateCtx < 4.0) {
            context.set(ctx2index.get(SENSING_RATE),3);
            return;
        };

        if (sensingRateCtx < 5.0) {
            context.set(ctx2index.get(SENSING_RATE),4);
            return;
        };
    }

    public void setBatteryLevelCtx(double batteryLevelCtx) {
        this.batteryLevelCtx = batteryLevelCtx;

        context.set(ctx2index.get(BATTERY_LEVEL), (int) (batteryLevelCtx/0.2));
    }

    public void setNextdayForecastCtx(double forecastCtx) {
        this.nextdayForecastCtx = forecastCtx;
        context.set(ctx2index.get(NEXTDAY_FORECAST), (int) (forecastCtx/0.35));
    }

    public void setTodayForecastCtx(double forecastCtx) {
        this.todayForecastCtx = forecastCtx;
        context.set(ctx2index.get(NEXTDAY_FORECAST), (int) (forecastCtx/0.35));
    }

    public void setTimeCtx(int timeCtx) {
        this.timeCtx = timeCtx;

        if (timeCtx < 6){
            context.set(ctx2index.get(TIME), 0);
            return;
        }

        if (timeCtx < 12){
            context.set(ctx2index.get(TIME), 1);
            return;
        }

        if (timeCtx < 18){
            context.set(ctx2index.get(TIME), 2);
            return;
        }
        context.set(ctx2index.get(TIME), 0);
    }

}

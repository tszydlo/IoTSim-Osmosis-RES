package org.cloudbus.cloudsim.osmesis.examples;

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
    static String HOUR="hour";
    static String MONTH="part_of_year";

    double sensingRateCtx;
    double batteryLevelCtx;
    double nextdayForecastCtx;
    double todayForecastCtx;
    int hourCtx;
    int monthCtx;

    List<Integer> maxCtxElements;
    Map<String, Integer> ctx2index;
    List<String> ctxElements;

    List<Integer> context;

    public RES_RL_example1_Environment() {
        maxCtxElements = new ArrayList<>();
        ctx2index = new HashMap<>();
        ctxElements = new ArrayList<>();
        context = new ArrayList<>();

        //add_context_element(BATTERY_LEVEL, 5);
        //add_context_element(SENSING_RATE, 5);
        add_context_element(NEXTDAY_FORECAST, 3);
        add_context_element(TODAY_FORECAST, 3);
        //add_context_element(HOUR, 4);
        add_context_element(MONTH, 3);
    }

    private void add_context_element(String name, int max_num_of_elements){
        ctxElements.add(name);
        ctx2index.put(name,ctxElements.size()-1);
        maxCtxElements.add(max_num_of_elements);
        context.add(0);
    }

    @Override
    public double getReward() {
        if (batteryLevelCtx < 0.05){
            System.out.println("!!!!!! DRAINED BATTERY !!!!!!");
            return 0;
        } else {
            return batteryLevelCtx*0.8 + (60.0/sensingRateCtx)*0.2;
        }
    }

    @Override
    public int getState() {
        int state = 0;
        int mult = 1;
        for(int i =0; i< context.size(); i++){
            state+=context.get(i) * mult;
            mult*=maxCtxElements.get(i);
        }
        return state;
    }

    @Override
    public int getNumStates() {
        int states = 1;
        for(int i =0; i< context.size(); i++){
            states*=maxCtxElements.get(i);
        }
        return states;
    }

    public void setSensingRateCtx(double sensingRateCtx) {
        this.sensingRateCtx = sensingRateCtx;

        if (ctx2index.containsKey(SENSING_RATE)) {
            if (sensingRateCtx < 1.0) {
                context.set(ctx2index.get(SENSING_RATE), 0);
                return;
            }

            if (sensingRateCtx < 2.0) {
                context.set(ctx2index.get(SENSING_RATE), 1);
                return;
            }

            if (sensingRateCtx < 3.0) {
                context.set(ctx2index.get(SENSING_RATE), 2);
                return;
            }

            if (sensingRateCtx < 4.0) {
                context.set(ctx2index.get(SENSING_RATE), 3);
                return;
            }

            if (sensingRateCtx < 5.0) {
                context.set(ctx2index.get(SENSING_RATE), 4);
                return;
            }
        }
    }

    public void setBatteryLevelCtx(double batteryLevelCtx) {
        this.batteryLevelCtx = batteryLevelCtx;

        if (ctx2index.containsKey(BATTERY_LEVEL)) {
            int value = (int) (batteryLevelCtx / 0.2);

            if (value >= maxCtxElements.get(ctx2index.get(BATTERY_LEVEL))) {
                value = maxCtxElements.get(ctx2index.get(BATTERY_LEVEL)) - 1;
            }

            context.set(ctx2index.get(BATTERY_LEVEL), value);
        }
    }

    public void setNextdayForecastCtx(double forecastCtx) {
        this.nextdayForecastCtx = forecastCtx;
        context.set(ctx2index.get(NEXTDAY_FORECAST), (int) (forecastCtx/0.35));
    }

    public void setTodayForecastCtx(double forecastCtx) {
        this.todayForecastCtx = forecastCtx;
        context.set(ctx2index.get(TODAY_FORECAST), (int) (forecastCtx/0.35));
    }

    public void setMonthCtx(int monthCtx) {
        this.monthCtx = monthCtx;

        if (monthCtx == 1 || monthCtx == 2 || monthCtx == 11 || monthCtx == 12){
            context.set(ctx2index.get(MONTH), 0);
            return;
         }

        if (monthCtx == 3 || monthCtx == 4 || monthCtx == 9 || monthCtx == 10){
            context.set(ctx2index.get(MONTH), 1);
            return;
        }

        if (monthCtx == 5 || monthCtx == 6 || monthCtx == 7 || monthCtx == 8){
            context.set(ctx2index.get(MONTH), 2);
            return;
        }
    }

    public void setHourCtx(int hourCtx) {
        this.hourCtx = hourCtx;

        if (ctx2index.containsKey(HOUR)) {
            if (hourCtx < 6) {
                context.set(ctx2index.get(HOUR), 0);
                return;
            }

            if (hourCtx < 12) {
                context.set(ctx2index.get(HOUR), 1);
                return;
            }

            if (hourCtx < 18) {
                context.set(ctx2index.get(HOUR), 2);
                return;
            }
            context.set(ctx2index.get(HOUR), 0);
        }
    }
}

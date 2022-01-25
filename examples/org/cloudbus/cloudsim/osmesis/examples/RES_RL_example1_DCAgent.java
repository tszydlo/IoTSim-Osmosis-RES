package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.AgentMessage;
import org.cloudbus.agent.DCAgent;
import org.cloudbus.agent.qlearning.QLearning;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.res.dataproviders.ForecastData;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RES_RL_example1_DCAgent extends DCAgent {

    List<AgentMessage> messages;

    RES_RL_example1_Environment environment;

    QLearning qLearning;

    ForecastData forecast;

    private int previousActionId;
    private double previousReward;
    private int currentState;

    private double cumulatedReward=0;
    private int cumulative_low_bat=0;

    private Set<Integer> lowBatteryDays;

    double sensing_sum=0;
    double sensing_iter=0;

    boolean stored;

    Random random;
    private static final int DEFAULT_NUMBER_OF_ACTIONS = 6;

    public RES_RL_example1_DCAgent() {
        environment = new RES_RL_example1_Environment();
        qLearning = new QLearning(environment.getNumStates(), DEFAULT_NUMBER_OF_ACTIONS, true);
        qLearning.setEpsilon(0.0);
        random = new Random();
        lowBatteryDays = new HashSet<>();
    }

    @Override
    public void monitor() {
        super.monitor();

        //Read model on the first day
        int d = energyController.getSimulationCurrentTime().getDayOfYear();
        int h = energyController.getSimulationCurrentTime().getHour();

        if (d==1 && h==0 && !stored) {
            qLearning.readQ("q_table_"+getName()+".json");
            stored = true;
        }
    }

    @Override
    public void analyze() {
        super.analyze();
    }

    @Override
    public void plan() {
        super.plan();

        messages = getReceivedMessages();

        for(AgentMessage message:messages) {
            RES_RL_example1_AgentMessage res_rl_message = (RES_RL_example1_AgentMessage) message;

            if (res_rl_message.isToEdge()){
                //System.out.println("Message from device "+res_rl_message.getSOURCE()+" Battery Level = "+res_rl_message.getBatteryLevel());

                environment.setBatteryLevelCtx(res_rl_message.getBatteryLevel());
                environment.setSensingRateCtx(res_rl_message.getSensingRate());

                if (res_rl_message.getSensingRate() == 0.0){
                    System.out.println("ASSERT " + res_rl_message.getSensingRate());
                }

                if (energyController.getEnergySources().get(0).getEnergyData().isForecast()){
                    forecast = (ForecastData) energyController.getEnergySources().get(0).getEnergyData();
                    double forecast_tab[] = forecast.getNDayForecast(2, energyController.getSimulationCurrentTime());
                    environment.setTodayForecastCtx(forecast_tab[0]);
                    environment.setNextdayForecastCtx(forecast_tab[1]);
                };

                environment.setHourCtx(energyController.getSimulationCurrentTime().getHour());
                environment.setMonthCtx(energyController.getSimulationCurrentTime().getMonthValue());

                this.previousReward = environment.getReward();
                this.currentState = environment.getState();

                sensing_iter += 1.0;
                sensing_sum += res_rl_message.getSensingRate();

                cumulatedReward += previousReward;

                if (previousReward == 0.0) {
                    lowBatteryDays.add(energyController.getSimulationCurrentTime().getDayOfYear());
                }

                if (previousReward == 0.0) {
                    //allow for terminal states
                    this.qLearning.setQforAction(this.previousActionId, 0.0);
                } else {
                    this.qLearning.update(this.previousActionId, this.currentState, this.previousReward);
                }


                String line = String.format("%1s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s"
                        , energyController.getSimulationCurrentTime().getYear()
                        , energyController.getSimulationCurrentTime().getDayOfYear()
                        , energyController.getSimulationCurrentTime().getMonthValue()
                        , energyController.getSimulationCurrentTime().getHour()
                        , previousReward
                        , currentState
                        , previousActionId
                        , res_rl_message.getBatteryLevel()
                        , res_rl_message.getSensingRate());

                Log.printLine(line);

                try {
                    BufferedWriter writer = null;
                    writer = new BufferedWriter(new FileWriter("rl_battery.csv", true));
                    writer.append(line);
                    writer.append("\n");
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void execute() {
        super.execute();

        //Create new message.
        RES_RL_example1_AgentMessage res_rl_message = (RES_RL_example1_AgentMessage) newAgentMessage();
        res_rl_message.setToDevice();

        previousActionId =  qLearning.selectActionWithExploration();

        //For constant action selection
        //previousActionId = 5;

        int temp = DEFAULT_NUMBER_OF_ACTIONS - previousActionId - 1;
        res_rl_message.setSensingRate(temp * 30.0 + 60.0);

        res_rl_message.setDESTINATION(null);
        publishMessage(res_rl_message);

        //Store model on the last day
        int d = energyController.getSimulationCurrentTime().getDayOfYear();
        int h = energyController.getSimulationCurrentTime().getHour();

        if (d==365 && h==23 && stored) {
            stored = false;
            qLearning.saveQ("q_table_"+getName()+".json");

            System.out.println(">>>>>>>>>>>>> CUMULATED REWARD: "+ cumulatedReward);
            System.out.println(">>>>>>>>>>>>> Low Batt Days: "+ lowBatteryDays.size());
            System.out.println(">>>>>>>>>>>>> Average Sensing Rate: "+ sensing_sum/sensing_iter);
        }
    }
}

package org.cloudbus.cloudsim.osmesis.examples;

import com.github.chen0040.rl.learning.qlearn.QAgent;
import org.cloudbus.agent.AgentMessage;
import org.cloudbus.agent.DCAgent;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.res.dataproviders.ForecastData;

import java.util.List;

public class RES_RL_example1_DCAgent extends DCAgent {

    List<AgentMessage> messages;

    QAgent qAgent;
    RES_RL_example1_Environment environment;

    ForecastData forecast;

    private int previousActionId;
    private double previousReward;
    private int currentState;

    private static final int DEFAULT_NUMBER_OF_ACTIONS = 5;

    public RES_RL_example1_DCAgent() {
        environment = new RES_RL_example1_Environment();
        qAgent = new QAgent(environment.getNumStates(), DEFAULT_NUMBER_OF_ACTIONS);
    }

    @Override
    public void monitor() {
        super.monitor();

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

                if (energyController.getEnergySources().get(0).getEnergyData().isForecast()){
                    forecast = (ForecastData) energyController.getEnergySources().get(0).getEnergyData();
                    double forecast_tab[] = forecast.getNDayForecast(2, energyController.getSimulationCurrentTime());
                    environment.setTodayForecastCtx(forecast_tab[0]);
                    environment.setNextdayForecastCtx(forecast_tab[1]);
                };

                environment.setTimeCtx((int)(CloudSim.clock()%(24*3600)/3600));

                this.previousReward = environment.getReward();
                this.currentState = environment.getState();
                this.qAgent.update(this.previousActionId, this.currentState, this.previousReward);


                Log.printLine(String.format("%1s\t%10s\t%10s\t%10s\t%10s\t%10s\t%10s"
                        , (int)(CloudSim.clock()/3600/24)
                        , (int)(CloudSim.clock()%(24*3600)/3600)
                        , previousReward
                        , currentState
                        , previousActionId
                        , res_rl_message.getBatteryLevel()
                        , res_rl_message.getSensingRate()));
            }
        }
    }

    @Override
    public void execute() {
        super.execute();

        //Create new message.
        RES_RL_example1_AgentMessage res_rl_message = (RES_RL_example1_AgentMessage) newAgentMessage();
        res_rl_message.setToDevice();
        this.previousActionId =  qAgent.selectAction().getIndex();

        res_rl_message.setSensingRate(previousActionId * 20.0 + 100.0);
        //Send to all neighbours (null destination means all - follows the agent topology defined in the example file).
        res_rl_message.setDESTINATION(null);
        publishMessage(res_rl_message);
    }
}

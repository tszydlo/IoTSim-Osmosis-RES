package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.AgentBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.core.edge.ConfiguationEntity;
import org.cloudbus.cloudsim.edge.utils.LogUtil;
import org.cloudbus.cloudsim.osmesis.examples.uti.LogPrinter;
import org.cloudbus.cloudsim.osmesis.examples.uti.PrintResults;
import org.cloudbus.cloudsim.osmesis.examples.uti.RESPrinterDeviceBattery;
import org.cloudbus.cloudsim.sdn.Switch;
import org.cloudbus.osmosis.core.*;
import org.cloudbus.res.EnergyController;
import org.cloudbus.res.config.AppConfig;
import org.cloudbus.res.dataproviders.ForecastData;
import org.cloudbus.res.dataproviders.res.RESResponse;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RES_RL_example1 {
    //Workload and infrastructure configuration are the same as in the example 2.
    public static final String configurationFile = "inputFiles/res/RES_example7_infrastructure_2edges.json";
    public static final String osmesisAppFile =  "inputFiles/res/RES_RL_example1_workload.csv";
    //RES configuration is the same as in the example 6.
    public static final String RES_CONFIG_FILE =  "inputFiles/res/RES_example6_energy_config.json";
    //public static final String AGENT_CONFIG_FILE="inputFiles/agent/RES_example6_agent_config.json";

    OsmosisBuilder topologyBuilder;
    OsmesisBroker osmesisBroker;
    EdgeSDNController edgeSDNController;

    public static void main(String[] args) throws Exception {
        RES_RL_example1 osmosis = new RES_RL_example1();
        osmosis.start();
    }

    public void start() throws Exception{

        int num_user = 1; // number of users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Set Agent and Message classes
        AgentBroker agentBroker = AgentBroker.getInstance();

        // Set Agent and Message classes
        agentBroker.setDcAgentClass(RES_RL_example1_DCAgent.class);
        agentBroker.setDeviceAgentClass(RES_RL_example1_DeviceAgent.class);
        agentBroker.setAgentMessageClass(RES_RL_example1_AgentMessage.class);

        //Simulation is not started yet thus there is not any MELs.
        //Links for Agents between infrastructure elements.
        agentBroker.addAgentLink("temperature_1", "Edge_M_1");
        agentBroker.addAgentLink("temperature_1", "Edge_M_2");

        //Osmotic Agents time interval
        agentBroker.setMAPEInterval(15*60);

        // Getting configuration from json and entering classes to Agent Broker
        //AgentConfigProvider provider = new AgentConfigProvider(AgentConfigLoader.getFromFile(AGENT_CONFIG_FILE));

        // In this example, the Central Agent is not used
        //agentBroker.setDcAgentClass(provider.getDCAgentClass());
        //agentBroker.setDeviceAgentClass(provider.getDeviceAgentClass());
        //agentBroker.setDeviceAgentClass(RES_forecast_example1_DeviceAgent.class);
        //agentBroker.setAgentMessageClass(provider.getAgentMessageClass());

        //Simulation is not started yet thus there is not any MELs.
        //Links for Agents between infrastructure elements.
        //for (TopologyLink link : provider.getTopologyLinks()) {
        //    agentBroker.addAgentLink(link.AgentA, link.AgentB);
        //}

        //Osmotic Agents time interval
        agentBroker.setMAPEInterval(10*60);

        //Create Energy Controllers
        Map<String, EnergyController> energyControllers = getEnergyControllers();
        System.out.println(energyControllers);

        for(EnergyController eC: energyControllers.values()){
            if (eC.getEnergySources().get(0).getEnergyData().isForecast()){
                ((ForecastData)eC.getEnergySources().get(0).getEnergyData()).calculateForecast();
            };

        }

        agentBroker.setEnergyControllers(energyControllers);

        //Set the simulation start time
        String simulationStartTime="20160101:0000";
        //String simulationStartTime="20160501:0000";
        //String simulationStartTime="20160901:0000";

        agentBroker.setSimulationStartTime(simulationStartTime);

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        osmesisBroker  = new OsmesisBroker("OsmesisBroker");
        topologyBuilder = new OsmosisBuilder(osmesisBroker);

        ConfiguationEntity config = buildTopologyFromFile(configurationFile);
        //
        if(config !=  null) {
            topologyBuilder.buildTopology(config);
        }

        OsmosisOrchestrator maestro = new OsmosisOrchestrator();

        OsmesisAppsParser.startParsingExcelAppFile(osmesisAppFile);
        List<SDNController> controllers = new ArrayList<>();
        for(OsmesisDatacenter osmesisDC : topologyBuilder.getOsmesisDatacentres()){
            osmesisBroker.submitVmList(osmesisDC.getVmList(), osmesisDC.getId());
            controllers.add(osmesisDC.getSdnController());
            osmesisDC.getSdnController().setWanOorchestrator(maestro);
        }
        controllers.add(topologyBuilder.getSdWanController());
        maestro.setSdnControllers(controllers);
        osmesisBroker.submitOsmesisApps(OsmesisAppsParser.appList);
        osmesisBroker.setDatacenters(topologyBuilder.getOsmesisDatacentres());

        double startTime = CloudSim.startSimulation();

        LogUtil.simulationFinished();
        PrintResults pr = new PrintResults();
        pr.printOsmesisNetwork();

        Log.printLine();

        for(OsmesisDatacenter osmesisDC : topologyBuilder.getOsmesisDatacentres()){
            List<Switch> switchList = osmesisDC.getSdnController().getSwitchList();
            LogPrinter.printEnergyConsumption(osmesisDC.getName(), osmesisDC.getSdnhosts(), switchList, startTime);
            Log.printLine();
        }

        Log.printLine();
        LogPrinter.printEnergyConsumption(topologyBuilder.getSdWanController().getName(), null, topologyBuilder.getSdWanController().getSwitchList(), startTime);
        Log.printLine();
        Log.printLine("Simulation Finished!");

        Log.printLine();
        Log.printLine("Post-mortem RES energy analysis!");
        RESPrinterDeviceBattery res_printer = new RESPrinterDeviceBattery();
        res_printer.postMortemAnalysis(energyControllers,simulationStartTime, true,1);
        //res_printer.postMortemAnalysis(energyControllers,simulationStartTime, false, 36);
        //res_printer.postMortemAnalysis(energyControllers,"20160901:0000", false, 36);
        Log.printLine("End of RES analysis!");
    }

    private Map<String, EnergyController> getEnergyControllers() throws IOException {
        RESResponse resResponse = AppConfig.RES_PARSER.parse(RES_CONFIG_FILE);
        return resResponse.getDatacenters()
                .stream()
                .map(EnergyController::fromDatacenter)
                .collect(Collectors.toMap(EnergyController::getEdgeDatacenterId, Function.identity()));
    }

    private ConfiguationEntity buildTopologyFromFile(String filePath) {
        System.out.println("Creating topology from file " + filePath);
        ConfiguationEntity conf  = null;
        try (FileReader jsonFileReader = new FileReader(filePath)){
            conf = topologyBuilder.parseTopology(jsonFileReader);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: input configuration file not found");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Topology built:");
        return conf;
    }

    public void setEdgeSDNController(EdgeSDNController edc) {
        this.edgeSDNController = edc;
    }




}

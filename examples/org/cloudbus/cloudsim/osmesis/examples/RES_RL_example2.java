package org.cloudbus.cloudsim.osmesis.examples;

import org.cloudbus.agent.AgentBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.edge.core.edge.ConfiguationEntity;
import org.cloudbus.cloudsim.edge.utils.LogUtil;
import org.cloudbus.cloudsim.osmesis.examples.uti.LogPrinter;
import org.cloudbus.cloudsim.sdn.Switch;
import org.cloudbus.osmosis.core.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RES_RL_example2 {
    //Workload and infrastructure configuration are the same as in the example 2.
    public static final String configurationFile = "inputFiles/res/RES_RL_example2_infrastructure_1edge.json";
    public static final String osmesisAppFile = "inputFiles/res/RES_RL_example2_workload.csv";

    OsmosisBuilder topologyBuilder;
    OsmesisBroker osmesisBroker;
    EdgeSDNController edgeSDNController;

    public static void main(String[] args) throws Exception {
        RES_RL_example2 osmosis = new RES_RL_example2();
        osmosis.start();
    }

    public void start() throws Exception {

        int num_user = 1; // number of users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events

        // Set Agent and Message classes
        AgentBroker agentBroker = AgentBroker.getInstance();

        // Set Agent and Message classes
        agentBroker.setDcAgentClass(RES_RL_example2_DCAgent.class);
        agentBroker.setDeviceAgentClass(RES_RL_example2_DeviceAgent.class);
        agentBroker.setAgentMessageClass(RES_RL_example2_AgentMessage.class);

        //Simulation is not started yet thus there is not any MELs.
        //Links for Agents between infrastructure elements.
        agentBroker.addAgentLink("sensor0", "Edge_M_1");
        agentBroker.addAgentLink("sensor1", "Edge_M_1");
        agentBroker.addAgentLink("sensor2", "Edge_M_1");
        agentBroker.addAgentLink("sensor3", "Edge_M_1");

        //Osmotic Agents time interval
        agentBroker.setMAPEInterval(15 * 60);

        JSONParser parser = new JSONParser();
        String clientId = UUID.randomUUID().toString();
        RL_BROKER.mqttClient = new MqttClient("tcp://127.0.0.1:1883", clientId);
        RL_BROKER.mqttClient.connect();
        RL_BROKER.mqttClient.subscribe(RL_BROKER.mqttTopicRange, (topic, msg) -> {
            Map<Integer, Integer> parsedData = (Map<Integer, Integer>) parser.parse(new String(msg.getPayload()));
            System.out.println("GOT NEW MESSAGE");
            System.out.println(parsedData);
            RL_BROKER.newSensingRangeMessage(parsedData);
        });

        //Set the simulation start time
        String simulationStartTime = "20160101:0000";
        //String simulationStartTime="20160501:0000";
        //String simulationStartTime="20160901:0000";

        agentBroker.setSimulationStartTime(simulationStartTime);

        // Initialize the CloudSim library
        CloudSim.init(num_user, calendar, trace_flag);
        osmesisBroker = new OsmesisBroker("OsmesisBroker");
        topologyBuilder = new OsmosisBuilder(osmesisBroker);

        ConfiguationEntity config = buildTopologyFromFile(configurationFile);
        //
        if (config != null) {
            topologyBuilder.buildTopology(config);
        }

        OsmosisOrchestrator maestro = new OsmosisOrchestrator();

        OsmesisAppsParser.startParsingExcelAppFile(osmesisAppFile);
        List<SDNController> controllers = new ArrayList<>();
        for (OsmesisDatacenter osmesisDC : topologyBuilder.getOsmesisDatacentres()) {
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

        Log.printLine();

        for (OsmesisDatacenter osmesisDC : topologyBuilder.getOsmesisDatacentres()) {
            List<Switch> switchList = osmesisDC.getSdnController().getSwitchList();
            LogPrinter.printEnergyConsumption(osmesisDC.getName(), osmesisDC.getSdnhosts(), switchList, startTime);
            Log.printLine();
        }

        Log.printLine();
    }


    private ConfiguationEntity buildTopologyFromFile(String filePath) {
        System.out.println("Creating topology from file " + filePath);
        ConfiguationEntity conf = null;
        try (FileReader jsonFileReader = new FileReader(filePath)) {
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


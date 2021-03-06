/*
 * Title:        IoTSim-Osmosis 1.0
 * Description:  IoTSim-Osmosis enables the testing and validation of osmotic computing applications 
 * 			     over heterogeneous edge-cloud SDN-aware environments.
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2020, Newcastle University (UK) and Saudi Electronic University (Saudi Arabia) 
 * 
 */

package org.cloudbus.cloudsim.edge.iot;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.agent.AgentBroker;
import org.cloudbus.agent.DeviceAgent;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.edge.core.edge.Battery;
import org.cloudbus.cloudsim.edge.core.edge.Mobility;
import org.cloudbus.cloudsim.edge.iot.network.EdgeNetworkInfo;
import org.cloudbus.cloudsim.edge.utils.LogUtil;
import org.cloudbus.osmosis.core.*;
import org.cloudbus.osmosis.core.polocies.MovingPolicy;

/**
 * 
 * @author Khaled Alwasel
 * @contact kalwasel@gmail.com
 * @since IoTSim-Osmosis 1.0
 * 
**/

public abstract class IoTDevice extends SimEntity {

	public static int cloudLetId = 0;
	private double runningTime = 0;	
	public String sensed_data_type;
	protected Battery battery;
	private EdgeNetworkInfo networkModel;	
	private MovingPolicy movingPolicy;
	private Mobility mobility;
	int connectingEdgeDeviceId = -1;
	private boolean enabled;
	public abstract boolean updateBatteryBySensing();
	public abstract boolean updateBatteryByTransmission();
	private double bw;
	private double usedBw;

	String associatedEdge;

	private OsmosisRoutingTable routingTable = new OsmosisRoutingTable();
	private DeviceAgent osmoticDeviceAgent;

	private List<Flow> flowList = new ArrayList<>(); 
	
	public IoTDevice( String name, EdgeNetworkInfo networkModel, double bandwidth) {
		super(name);
		this.battery = new Battery();	
		this.networkModel = networkModel;
		this.enabled = true;		
		this.bw = bandwidth;

		//Osmosis Agents
		AgentBroker.getInstance().createDeviceAgent(name, this);
	}
	
	@Override
	public void startEntity() {		
	}

	public String getAssociatedEdge() {
		return associatedEdge;
	}

	public void setAssociatedEdge(String associatedEdge) {
		this.associatedEdge = associatedEdge;
	}

	@Override
	public void processEvent(SimEvent ev) {
		int tag = ev.getTag();
		switch (tag) {
		case OsmosisTags.SENSING:
			this.sensing(ev);
			break;
			
		case  OsmosisTags.updateIoTBW:
			this.removeFlow(ev);
			break;

		case OsmosisTags.MOVING:
			/*
			 * To do list 
			 */
			break;
		}
	}
	
	public double getRunningTime() {
		return this.runningTime;
	}

	public void setRunningTime(double runningTime) {
		this.runningTime = runningTime;
	}

	
	public Mobility getMobility() {
		return this.mobility;
	}
	
	public void setMobility(Mobility location) {
		this.mobility = location;
	}
	
	public Battery getBattery() {
		return this.battery;
	}	

	public MovingPolicy getMovingPolicy() {
		return this.movingPolicy;
	}

	public void setMovingPolicy(MovingPolicy movingPolicy) {
		this.movingPolicy = movingPolicy;
	}

	public void setEdgeDeviceId(int id) {
		this.connectingEdgeDeviceId = id;
	}

	public EdgeNetworkInfo getNetworkModel() {
		return this.networkModel;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {

		this.enabled = enabled;
	}
	
	private void sensing(SimEvent ev) {
		OsmesisAppDescription app = (OsmesisAppDescription) ev.getData();

		// if the battery is drained,
		this.updateBatteryBySensing();
		boolean died = this.updateBatteryByTransmission();
		app.setIoTBatteryConsumption(this.battery.getBatteryTotalConsumption());
		if (died) {
			app.setIoTDeviceDied(true);
			LogUtil.info(this.getClass().getSimpleName() + " running time is " + CloudSim.clock());

			this.setEnabled(false);
			LogUtil.info(this.getClass().getSimpleName()+" " + this.getId() + "'s battery has been drained");
			this.runningTime = CloudSim.clock();			
			return;
		}

		Flow flow = this.createFlow(app);	
		
		WorkflowInfo workflowTag = new WorkflowInfo();
		workflowTag.setStartTime(CloudSim.clock());
		workflowTag.setAppId(app.getAppID());
		workflowTag.setAppName(app.getAppName());
		workflowTag.setIotDeviceFlow(flow);
		workflowTag.setWorkflowId(app.addWorkflowId(1));
		workflowTag.setSourceDCName(app.getEdgeDatacenterName());
		workflowTag.setDestinationDCName(app.getCloudDatacenterName());
		flow.setWorkflowTag(workflowTag);
		OsmesisBroker.workflowTag.add(workflowTag);
		flow.addPacketSize(app.getIoTDeviceOutputSize());			
		updateBandwidth();



		//Adaptive Osmosis Flow Routing
		String finalMEL = routingTable.getRule(flow.getAppNameDest());
		flow.setAppNameDest(finalMEL);

		//MEL ID Resolution in Osmotic Broker
		//sendNow(flow.getDatacenterId(), OsmosisTags.TRANSMIT_IOT_DATA, flow);
		sendNow(OsmesisBroker.brokerID, OsmosisTags.ROUTING_MEL_ID_RESOLUTION, flow); //necessary for osmotic flow routing - concept similar to ARP protocol
	}


	private Flow createFlow(OsmesisAppDescription app) {
		//melID will be set in the osmosis broker in the MEL_ID_RESOLUTION process.
		int melId = -1;
		int datacenterId = -1;
		datacenterId = app.getEdgeDcId();					
		int id = OsmosisBuilder.flowId ;
		Flow flow  = new Flow(this.getName(),app.getMELName(), this.getId(), melId, id, null);
		//Flow flow  = new Flow(this.getName(),mel_name, this.getId(), melId, id, null);
		flow.setOsmesisAppId(app.getAppID());
		flow.setAppName(app.getAppName());		
		flow.addPacketSize(app.getIoTDeviceOutputSize());
		flow.setSubmitTime(CloudSim.clock());
		flow.setDatacenterId(datacenterId);
		flow.setOsmesisEdgeletSize(app.getOsmesisEdgeletSize());
		OsmosisBuilder.flowId++;
		flowList.add(flow);
		
		return flow;
	}
	
	public void setBw(double bw) {
		this.bw = bw;
	}
	
	public double getBw() {
		return bw;
	}
	
	public double getUsedBw() {
		return usedBw;
	}
	
	public void removeFlow(SimEvent ev) {
		
		Flow flow  = (Flow) ev.getData();
		this.flowList.remove(flow);
		
		updateBandwidth();	
	}
	
	private void updateBandwidth(){			
		this.usedBw = this.getBw() / this.flowList.size(); // the updated bw 		
		for(Flow getFlow : this.flowList){
			getFlow.updateSourceBw(this.usedBw);
		}	
	}

	public OsmosisRoutingTable getRoutingTable() {
		return routingTable;
	}
}
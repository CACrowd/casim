package matsimconnector.engine;

import matsimconnector.agents.Pedestrian;
import matsimconnector.environment.TransitionArea;
import matsimconnector.scenario.CAEnvironment;
import matsimconnector.utility.IdUtility;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import pedca.engine.AgentsGenerator;
import pedca.environment.grid.GridPoint;

import java.util.HashMap;
import java.util.Map;

public class CAAgentFactory {

	//private Scenario scenario;
	private Map<Id<CAEnvironment>, AgentsGenerator> generators;
	
	public CAAgentFactory() {
		this.generators = new HashMap<Id<CAEnvironment>,AgentsGenerator>();
	}
	
	public Pedestrian buildPedestrian(Id<CAEnvironment> environmentId, QVehicle vehicle, TransitionArea transitionArea){
		GridPoint gp = transitionArea.calculateEnterPosition();
		int destinationId = extractDestinationId(vehicle);
		Pedestrian pedestrian = generators.get(environmentId).generatePedestrian(gp, destinationId, vehicle,transitionArea);
		return pedestrian;
	}

	private int extractDestinationId(QVehicle vehicle) {
		Id<Link> linkId = vehicle.getDriver().chooseNextLinkId();
		return IdUtility.linkIdToDestinationId(linkId);
	}
	
	protected void addAgentsGenerator(Id<CAEnvironment> environmentId, AgentsGenerator agentGenerator){
		this.generators.put(environmentId,agentGenerator);
	}
	
}

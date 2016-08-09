/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.matsimconnector.engine;

import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.matsimconnector.utility.IdUtility;
import org.cacrowd.casim.pedca.engine.AgentsGenerator;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

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

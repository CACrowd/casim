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

package org.cacrowd.casim.pedca.engine;

import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

public class AgentsGenerator {

	private Context context;
	private int pedestrianCounter;
	
	public AgentsGenerator(Context context){
		this.context = context;
		pedestrianCounter = 0;
	}

	//FOR MATSIM CONNECTOR
	public Pedestrian generatePedestrian(GridPoint initialPosition, int destinationId, QVehicle vehicle, TransitionArea transitionArea, String envCAId){
		int pedID = pedestrianCounter;
		Destination destination = context.getMarkerConfiguration().getDestination(destinationId);
		Pedestrian pedestrian = new Pedestrian(pedID, initialPosition, destination, context, vehicle, transitionArea, envCAId);
		context.getPopulation().addPedestrian(pedestrian);
		pedestrianCounter++;
		return pedestrian;
	}
	
	//FOR MATSIM CONNECTOR
	public Context getContext(){
		return context;
	}

}

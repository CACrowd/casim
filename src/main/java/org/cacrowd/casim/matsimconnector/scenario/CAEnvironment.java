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

package org.cacrowd.casim.matsimconnector.scenario;


import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.markers.FinalDestination;
import org.cacrowd.casim.pedca.environment.network.CANetwork;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import java.util.HashMap;
import java.util.Map;

//TODO: extract interface outside matsim package [gl May 16]
public class CAEnvironment {
	private Id<CAEnvironment> id;
	private Context context;
	private Network network;
	private Map <Id<Link>,TransitionArea> transitionAreas;
	
	public CAEnvironment(String id, Context context){
		this(Id.create(id,CAEnvironment.class),context);
	}


    public CAEnvironment(Id<CAEnvironment> id, Context context){
		this.id = id;
		this.context = context;
		this.transitionAreas = new HashMap<Id<Link>,TransitionArea>();
	}

	public Id<CAEnvironment> getId(){
		return id;
	}
	
	public Context getContext(){
		return context;
	}
	
	public CANetwork getCANetwork(){
		return context.getNetwork();
	}
	
	public Network getNetwork(){
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}
	
	public void addTransitionArea(Id<Link> linkId, TransitionArea transitionArea){
		transitionAreas.put(linkId, transitionArea);
	}
	
	public Map<Id<Link>,TransitionArea> getTransitionAreas(){
		return transitionAreas;
	}
	
	public FinalDestination getDestination(int destinationId){
		return (FinalDestination) getContext().getMarkerConfiguration().getDestination(destinationId);
	}

}

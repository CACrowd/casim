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

package org.cacrowd.casim.matsimconnector.utility;

import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.pedca.environment.markers.FinalDestination;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.mobsim.qsim.qnetsimengine.QCALink;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

import java.util.Set;

public class LinkUtility {
	public static void initLink(Link link, double length, Set<String> modes){
		link.setLength(length);
		link.setFreespeed(Constants.PEDESTRIAN_SPEED);
		
		//TODO FIX THE FLOW CAPACITY
		double width = Constants.FAKE_LINK_WIDTH;
		double cap = length* Constants.FLOPW_CAP_PER_METER_WIDTH;
		//double cap = width*Constants.FLOPW_CAP_PER_METER_WIDTH;
		link.setCapacity(cap);
		link.setAllowedModes(modes);
	}

	public static void initLink(Link link, double length, int lanes,Set<String> modes){
		initLink(link, length, modes);
		link.setNumberOfLanes(lanes);
	}
	
	public static int getTransitionAreaWidth(Node borderNode, CAEnvironment environmentCA){
		int destinationId = IdUtility.nodeIdToDestinationId(borderNode.getId());
		FinalDestination tacticalDestination = (FinalDestination)environmentCA.getContext().getMarkerConfiguration().getDestination(destinationId);
		return (int)(tacticalDestination.getWidth()/ Constants.CA_CELL_SIDE);
	}
	
	public static double getTransitionLinkWidth(Link link, CAEnvironment environmentCA){
		int destinationId = IdUtility.linkIdToDestinationId(link.getId());
		return ((FinalDestination)environmentCA.getContext().getMarkerConfiguration().getDestination(destinationId)).getWidth();
	}
	
	public static int getTransitionAreaWidth(Link link, CAEnvironment env) {
		return (int)(getTransitionLinkWidth(link, env)/ Constants.CA_CELL_SIDE);
	}
	
	public static TransitionArea getDestinationTransitionArea(QVehicle vehicle){
		Node borderNode = vehicle.getCurrentLink().getFromNode();
		for (Link link : borderNode.getInLinks().values())
			if (link instanceof QCALink)
				return ((QCALink)link).getTransitionArea();
		
		throw new RuntimeException("QCALink not found!!!");
	}
}
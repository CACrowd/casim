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

import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;

public class IdUtility {
	private static String nodeIdPrefix = "HN_x_";
	
	public static Id<Link> createLinkId(int environmentCAId, int fromCANodeId, int toCANodeId) {
		return createLinkId(createNodeId(fromCANodeId,environmentCAId), createNodeId(toCANodeId,environmentCAId));
	}
	
	public static Id<Link> createLinkId(Id<Node> fromId, Id<Node> toId) {
		return Id.create(fromId.toString() + "-->"+toId.toString(),Link.class);
	}
	
	public static Id<Node> createNodeId(int CANodeId) {
		return Id.create(nodeIdPrefix+CANodeId,Node.class);
	}
	
	public static Id<Node> createNodeId(int CANodeId, int environmentCAId) {
		return Id.create(nodeIdPrefix.substring(0, nodeIdPrefix.lastIndexOf('_')-1)+environmentCAId+"_"+CANodeId,Node.class);
	}
	
	public static int nodeIdToDestinationId(Id<Node> nodeId){
		return Integer.parseInt(nodeId.toString().substring(nodeIdPrefix.length()));
	}
	
	public static int linkIdToDestinationId(Id<Link> linkId){
		int beginIndex = linkId.toString().indexOf('>')+nodeIdPrefix.length()+1;
		return Integer.parseInt(linkId.toString().substring(beginIndex));
	}
	
	public static String linkIdToDestinationNodeId(String linkId){
		int beginIndex = linkId.toString().indexOf('>')+1;
		return linkId.toString().substring(beginIndex);
	}
	
	public static Id<Pedestrian> createPedestrianId(String envCAId, int pedId){
		return Id.create("p_"+envCAId+"_"+pedId, Pedestrian.class);
	}
	
	public static int getEnvironmentId (Id<Pedestrian> pedId){
		String id = pedId.toString();
		return Integer.parseInt(id.substring(id.indexOf("_")+1, id.lastIndexOf("_")));
	}
	
}

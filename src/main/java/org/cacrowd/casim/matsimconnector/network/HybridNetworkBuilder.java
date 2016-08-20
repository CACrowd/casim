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

package org.cacrowd.casim.matsimconnector.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.matsimconnector.scenario.CAScenario;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.utility.IdUtility;
import org.cacrowd.casim.matsimconnector.utility.MathUtility;
import org.cacrowd.casim.pedca.environment.network.CAEdge;
import org.cacrowd.casim.pedca.environment.network.CANode;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkFactoryImpl;
import org.matsim.core.network.NetworkUtils;

public class HybridNetworkBuilder {

	
	public static void buildNetwork(CAEnvironment environmentCA, CAScenario scenarioCA) {
		buildNetwork(environmentCA, scenarioCA, 0);
	}
	
	/**
	 * Creates a matsim Network from the CANetwork in the CAEnvironment input object
	 * */
	public static void buildNetwork(CAEnvironment environmentCA, CAScenario scenarioCA, int environmentCAId) {
        Network net = NetworkUtils.createNetwork();
        environmentCA.setNetwork(net);

        //////THIS LIST CONTAINS ID OF LINKS THAT WILL NOT BE ADDED IN THE MATSIM NETWORK
		ArrayList<String> linkIdBlackList = new ArrayList<String>();
		//linkIdBlackList.add("HybridNode_53-->HybridNode_12");
		
		//net.setCapacityPeriod(1);
		//net.setEffectiveCellSize(.26);
		//net.setEffectiveLaneWidth(.71);
		
		Set<String> modes = new HashSet<String>();
		modes.add(Constants.CAR_LINK_MODE);
		modes.add(Constants.WALK_LINK_MODE);
		modes.add(Constants.CA_LINK_MODE);
	
		NetworkFactory netFac = new NetworkFactoryImpl(net);
		
		for (CANode nodeCA : environmentCA.getCANetwork().getNodes()) {
			Id<Node> id = IdUtility.createNodeId(nodeCA.getId(), environmentCAId);
			double[] nodeShift = {environmentCA.getContext().environmentOrigin.getX(), environmentCA.getContext().environmentOrigin.getY()};
			double x = nodeCA.getCoordinate().getX()+nodeShift[0];
			double y = nodeCA.getCoordinate().getY()+nodeShift[1];
			Coordinate rotatedCoordinate = new Coordinate(x,y);
			MathUtility.rotate(rotatedCoordinate, environmentCA.getContext().environmentRotation, nodeShift[0], nodeShift[1]);	
			Node node = netFac.createNode(id, new Coord(rotatedCoordinate.getX(), rotatedCoordinate.getY()));
            net.addNode(node);
		}

		for (CAEdge edgeCA : environmentCA.getCANetwork().getEdges()) {
			Id<Node> fromId = IdUtility.createNodeId(edgeCA.getN1().getId(), environmentCAId);
			Id<Node> toId = IdUtility.createNodeId(edgeCA.getN2().getId(), environmentCAId);
			Node from = net.getNodes().get(fromId);
			Node to = net.getNodes().get(toId);
				
			Id <Link> linkId = IdUtility.createLinkId(fromId, toId);
			if (!(linkIdBlackList.contains(linkId.toString()))){
				if (edgeCA.isStairs())
					Constants.stairsLinks.add(linkId.toString());


                //TODO FIX THE FLOW CAPACITY
				double width = Constants.FAKE_LINK_WIDTH;
				//double lanes = width/net.getEffectiveLaneWidth();
				double cap = width* Constants.FLOPW_CAP_PER_METER_WIDTH;
                Link link = netFac.createLink(linkId, from, to); //, net, edgeCA.getLength(), Constants.PEDESTRIAN_SPEED, cap, 1);
                link.setLength(edgeCA.getLength());
                link.setFreespeed(Constants.PEDESTRIAN_SPEED);
                link.setCapacity(cap);
                link.setNumberOfLanes(1);
                link.setAllowedModes(modes);

                net.addLink(link);
				scenarioCA.mapLinkToEnvironment(link, environmentCA);
			}
		}
	}

}

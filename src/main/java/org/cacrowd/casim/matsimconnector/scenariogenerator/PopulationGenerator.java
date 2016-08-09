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

package org.cacrowd.casim.matsimconnector.scenariogenerator;

import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;

import java.util.ArrayList;

public class PopulationGenerator {

	public static void createPopulation(Scenario sc, int populationSize) {
		Network network = sc.getNetwork();
		ArrayList <Link> initLinks = new ArrayList<Link>();
		ArrayList <Link> destinationLinks = new ArrayList<Link>();
		for (Node node : network.getNodes().values()){
			if (isOriginNode(node)){
				initLinks.add(node.getOutLinks().values().iterator().next());
			}
			if(isDestinationNode(node)){
				destinationLinks.add(node.getOutLinks().values().iterator().next());
			}
		}
				
		Population population = sc.getPopulation();
		population.getPersons().clear();
		PopulationFactory factory = population.getFactory();
		double t = 0;
		double flowProportion = 1./initLinks.size();
		int generated = 0;
		for (Link link : initLinks){
			int linkLimit = (int)(populationSize*flowProportion);
			/*HOOGENDOORN EXP CONFIGURATION
			linkLimit = 2000;
			populationSize=4000;
			String originNodeId = link.getFromNode().getId().toString();
			if (originNodeId.endsWith("s")){
				linkLimit = populationSize-linkLimit;
				double cap = 5.*Constants.FLOPW_CAP_PER_METER_WIDTH;
				link.setCapacity(cap);
			}*/
			for (int i = 0; i < linkLimit & generated<populationSize; i++) {
				Person pers = factory.createPerson(Id.create("p"+population.getPersons().size(),Person.class));
				Plan plan = factory.createPlan();
				pers.addPlan(plan);
				Activity act0;
				act0 = factory.createActivityFromLinkId("origin", link.getId());
				act0.setEndTime(t);
				plan.addActivity(act0);
				Leg leg = factory.createLeg("car");
				plan.addLeg(leg);
				Activity act1 = factory.createActivityFromLinkId("destination", getDestinationLinkId(link,destinationLinks));
				plan.addActivity(act1);
				population.addPerson(pers);
				++generated;
			}
		}
	}

	private static Id<Link> getDestinationLinkId(Link originLink, ArrayList<Link> destinationLinks) {
		String originNodeId = originLink.getFromNode().getId().toString();
		if (originNodeId.endsWith("2n"))
			for (Link link : destinationLinks)
				if (link.getFromNode().getId().toString().endsWith("n")&&!link.getFromNode().getId().toString().endsWith("2n")) //s"))
					return link.getFromNode().getInLinks().values().iterator().next().getId();
		if (originNodeId.endsWith("s"))
			for (Link link : destinationLinks)
				if (link.getFromNode().getId().toString().endsWith("n"))
					return link.getFromNode().getInLinks().values().iterator().next().getId();
		if (originNodeId.endsWith("w"))
			for (Link link : destinationLinks)
				if (link.getFromNode().getId().toString().endsWith("e"))
					return link.getFromNode().getInLinks().values().iterator().next().getId();
		if (originNodeId.endsWith("e"))
			for (Link link : destinationLinks)
				if (link.getFromNode().getId().toString().endsWith("w"))
					return link.getFromNode().getInLinks().values().iterator().next().getId();
		return null;
	}

	private static boolean isOriginNode(Node node) {
		boolean result = false;
		for (int i = 0; !result && i < Constants.ORIGIN_FLOWS.length; i++) {
			result=node.getId().toString().endsWith(""+Constants.ORIGIN_FLOWS[i]);
		}				
		return result;
		//return node.getId().toString().endsWith("n")||node.getId().toString().endsWith("s")||node.getId().toString().endsWith("w")||node.getId().toString().endsWith("e");
	}
	
	private static boolean isDestinationNode(Node node) {
		return node.getId().toString().endsWith("n")||node.getId().toString().endsWith("s")||node.getId().toString().endsWith("w")||node.getId().toString().endsWith("e");
	}

	protected static void createCorridorPopulation(Scenario sc, int populationSize){
		Population population = sc.getPopulation();
		population.getPersons().clear();
		PopulationFactory factory = population.getFactory();
		double t = 0;
		double leftFlowProportion = 1.;
		int limit = (int)(populationSize*leftFlowProportion);
		for (int i = 0; i < limit; i++) {
			Person pers = factory.createPerson(Id.create("b"+i,Person.class));
			Plan plan = factory.createPlan();
			pers.addPlan(plan);
			Activity act0;
			act0 = factory.createActivityFromLinkId("origin", Id.create("l0",Link.class));
			act0.setEndTime(t);
			plan.addActivity(act0);
			Leg leg = factory.createLeg("car");
			plan.addLeg(leg);
			Activity act1 = factory.createActivityFromLinkId("destination", Id.create("l3",Link.class));
			plan.addActivity(act1);
			population.addPerson(pers);
		}
		for (int i = limit; i < populationSize; i++) {
			Person pers = factory.createPerson(Id.create("a"+i,Person.class));
			Plan plan = factory.createPlan();
			pers.addPlan(plan);
			Activity act0;
			act0 = factory.createActivityFromLinkId("origin", Id.create("l3Rev",Link.class));
			act0.setEndTime(t);
			plan.addActivity(act0);
			Leg leg = factory.createLeg("car");
			plan.addLeg(leg);
			Activity act1 = factory.createActivityFromLinkId("destination", Id.create("l0Rev",Link.class));
			plan.addActivity(act1);
			population.addPerson(pers);
		}
	}
}

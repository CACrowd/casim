/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016-2017 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 *
 */

package org.cacrowd.casim.matsimintegration.scenarios;


import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.cacrowd.casim.proto.HybridSimProto;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.HashSet;
import java.util.Set;

public class DiamondScenarioGenerator {
	
	
	/*    _________________________   scenarioHeigth
	 *   |			   |		   | A
	 * 	 |passagesWidth			   | |
	 * 	  			   |		     |
	 * 				   |		     | 
	 * 	 |			   |		   | |
	 * 	 |						   | |
	 * 	 |_____________|___________| v
	*	  <--------------------->   
	*			scenarioWidth
	*
	*/		
	private static double distanceToWalls = 1.2;
	
	
    public static HybridSimProto.Scenario generateScenario(Scenario sc, IdIntMapper mapper, double scenarioWidth, double scenarioHeight, double passagesWidth) {
        enrichConfig(sc.getConfig());
        createNetwork(sc, mapper, scenarioWidth, scenarioHeight, passagesWidth);
        createPopulation(sc);
        return createScenario(scenarioWidth, scenarioHeight, passagesWidth);
    }

    private static HybridSimProto.Scenario createScenario(double scenarioWidth, double scenarioHeight, double passagesWidth) {

        HybridSimProto.Scenario.Builder sb = HybridSimProto.Scenario.newBuilder();
        HybridSimProto.Edge.Builder eb = HybridSimProto.Edge.newBuilder();
        HybridSimProto.Coordinate.Builder cb = HybridSimProto.Coordinate.newBuilder();

        cb.setX(0);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(scenarioHeight);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());


        eb.setC0(cb.build());
        cb.setX(scenarioWidth);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        eb.setC0(cb.build());
        cb.setY(0.);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        eb.setC0(cb.build());
        cb.setX(0.);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        //BEGIN entrance
        cb.setX(0.5);
        cb.setY(scenarioHeight/2-passagesWidth*1.5);
        eb.setC0(cb.build());
        cb.setY(scenarioHeight/2+passagesWidth*1.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(0);
        sb.addEdges(eb.build());
                
        cb.setX(4.5);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(scenarioHeight/2-passagesWidth*1.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        eb.setC0(cb.build());
        cb.setY(scenarioHeight/2+passagesWidth*1.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(1);
        
        sb.addEdges(eb.build());
        
        eb.setC0(cb.build());
        cb.setY(scenarioHeight);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        cb.setX(0.5);
        cb.setY(scenarioHeight/2-(passagesWidth*1.5+0.4));
        eb.setC0(cb.build());
        cb.setX(4.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        cb.setX(0.5);
        cb.setY(scenarioHeight/2+(passagesWidth*1.5+0.4));
        eb.setC0(cb.build());
        cb.setX(4.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        //END entrance
        
        //BEGIN exit
        cb.setX(scenarioWidth-.4);
        cb.setY(scenarioHeight/2-passagesWidth*1.5);
        eb.setC0(cb.build());
        cb.setY(scenarioHeight/2+passagesWidth*1.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(5);
        sb.addEdges(eb.build());
        
        cb.setX(scenarioWidth - 4.5);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(scenarioHeight/2-passagesWidth*1.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        eb.setC0(cb.build());
        cb.setY(scenarioHeight/2+passagesWidth*1.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(4);
        sb.addEdges(eb.build());
        
        eb.setC0(cb.build());
        cb.setY(scenarioHeight);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
                
        cb.setX(scenarioWidth - 0.4);
        cb.setY(scenarioHeight/2-(passagesWidth*1.5+0.4));
        eb.setC0(cb.build());
        cb.setX(scenarioWidth - 4.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        cb.setX(scenarioWidth - 0.4);
        cb.setY(scenarioHeight/2+(passagesWidth*1.5+0.4));
        eb.setC0(cb.build());
        cb.setX(scenarioWidth - 4.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        //BEGIN center wall
        cb.setX(scenarioWidth/2);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(distanceToWalls);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        eb.setC0(cb.build());
        cb.setY(distanceToWalls+passagesWidth);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(2);
        sb.addEdges(eb.build());

        eb.setC0(cb.build());
        cb.setY(scenarioHeight-(distanceToWalls+passagesWidth));
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        eb.setC0(cb.build());
        cb.setY(scenarioHeight-distanceToWalls);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(3);
        sb.addEdges(eb.build());

        eb.setC0(cb.build());
        cb.setY(scenarioHeight);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());
        
        //END center wall

        return sb.build();
    }

    private static void enrichConfig(Config c) {

        c.controler().setLastIteration(100);
        c.controler().setWriteEventsInterval(1);
        c.qsim().setEndTime(3600);

        PlanCalcScoreConfigGroup.ActivityParams pre = new PlanCalcScoreConfigGroup.ActivityParams("origin");

        c.strategy().setMaxAgentPlanMemorySize(3);
        c.strategy().addParam("ModuleDisableAfterIteration_1", "50");
        c.strategy().addParam("Module_1", "ReRoute");
        c.strategy().addParam("ModuleProbability_1", "0.1");
        c.strategy().addParam("Module_2", "ChangeExpBeta");
        c.strategy().addParam("ModuleProbability_2", "0.9");

        c.travelTimeCalculator().setTravelTimeCalculatorType("TravelTimeCalculatorHashMap");
        //        c.travelTimeCalculator().setTravelTimeAggregatorType("experimental_LastMile");
        c.travelTimeCalculator().setTraveltimeBinSize(60);

        pre.setTypicalDuration(49); // needs to be geq 49, otherwise when
        // running a simulation one gets
        // "java.lang.RuntimeException: zeroUtilityDuration of type pre-evac must be greater than 0.0. Did you forget to specify the typicalDuration?"
        // the reason is the double precision. see also comment in
        // ActivityUtilityParameters.java (gl)
        pre.setMinimalDuration(49);
        pre.setClosingTime(49);
        pre.setEarliestEndTime(49);
        pre.setLatestStartTime(49);
        pre.setOpeningTime(49);

        PlanCalcScoreConfigGroup.ActivityParams post = new PlanCalcScoreConfigGroup.ActivityParams("destination");
        post.setTypicalDuration(49); // dito
        post.setMinimalDuration(49);
        post.setClosingTime(49);
        post.setEarliestEndTime(49);
        post.setLatestStartTime(49);
        post.setOpeningTime(49);
        c.planCalcScore().addActivityParams(pre);
        c.planCalcScore().addActivityParams(post);

        c.planCalcScore().setLateArrival_utils_hr(0.);
        c.planCalcScore().setPerforming_utils_hr(0.);
    }

    private static void createPopulation(Scenario sc) {
        Population pop = sc.getPopulation();
        PopulationFactory fac = pop.getFactory();
        for (int i = 0; i < 500; i++) {
            Person pers = fac.createPerson(Id.createPersonId(i));
            pop.addPerson(pers);
            Plan plan = fac.createPlan();
            pers.addPlan(plan);
            Activity a0;
            if (i%2 == 0)
            	a0 = fac.createActivityFromLinkId("origin", Id.createLinkId("originWest"));
            else
            	a0 = fac.createActivityFromLinkId("origin", Id.createLinkId("originEast"));
            a0.setEndTime(0);
            plan.addActivity(a0);
            Leg leg = fac.createLeg("car");
            plan.addLeg(leg);
            Activity a1;
            if (i%2 == 0)
            	a1 = fac.createActivityFromLinkId("destination", Id.createLinkId("destinationEast"));
            else
            	a1 = fac.createActivityFromLinkId("destination", Id.createLinkId("destinationWest"));
            plan.addActivity(a1);
        }
       
    }

    private static void createNetwork(Scenario sc, IdIntMapper idIntMapper, double scenarioWidth, double scenarioHeight, double passagesWidth) {
        Network net = sc.getNetwork();
        net.setCapacityPeriod(1);
        net.setEffectiveLaneWidth(0.71);
        net.setEffectiveCellSize(0.26);
        NetworkFactory fac = net.getFactory();
        
        Node nm1 = fac.createNode(Id.createNodeId(-1), CoordUtils.createCoord(-4.5, scenarioHeight/2));
        net.addNode(nm1);
        Node nw = fac.createNode(Id.createNodeId(0), CoordUtils.createCoord(.5, scenarioHeight/2));
        net.addNode(nw);
        Node n1 = fac.createNode(Id.createNodeId(1), CoordUtils.createCoord(4.5, scenarioHeight/2));
        net.addNode(n1);
        Node n2 = fac.createNode(Id.createNodeId(2), CoordUtils.createCoord(scenarioWidth/2, distanceToWalls+passagesWidth/2));
        net.addNode(n2);
        Node n3 = fac.createNode(Id.createNodeId(3), CoordUtils.createCoord(scenarioWidth/2, scenarioHeight - (distanceToWalls+passagesWidth/2)));
        net.addNode(n3);
        Node n4 = fac.createNode(Id.createNodeId(4), CoordUtils.createCoord(scenarioWidth-4.5, scenarioHeight/2));
        net.addNode(n4);
        Node ne = fac.createNode(Id.createNodeId(5), CoordUtils.createCoord(scenarioWidth-.5, scenarioHeight/2));
        net.addNode(ne);
        Node nm2 = fac.createNode(Id.createNodeId(-2), CoordUtils.createCoord(scenarioWidth+4.5, scenarioHeight/2));
        net.addNode(nm2);

        Link lm1_w = fac.createLink(Id.createLinkId("originWest"), nm1, nw);
        net.addLink(lm1_w);
        Link lw_m1 = fac.createLink(Id.createLinkId("destinationWest"), nw, nm1);
        net.addLink(lw_m1);
        
        Link lin_w = fac.createLink(Id.createLinkId("inWest"), nw, n1);
        net.addLink(lin_w);
        idIntMapper.addDestinationsLinkMapping(0, 1, lin_w);
        Link lout_w = fac.createLink(Id.createLinkId("outWest"), n1, nw);
        net.addLink(lout_w);
        idIntMapper.addDestinationsLinkMapping(1, 0, lout_w);
        
        Link l1_2 = fac.createLink(Id.createLinkId("1->2"), n1, n2);
        net.addLink(l1_2);
        idIntMapper.addDestinationsLinkMapping(1, 2, l1_2);
        Link l2_1 = fac.createLink(Id.createLinkId("2->1"), n2, n1);
        net.addLink(l2_1);
        idIntMapper.addDestinationsLinkMapping(2, 1, l2_1);
        
        Link l1_3 = fac.createLink(Id.createLinkId("1->3"), n1, n3);
        net.addLink(l1_3);
        idIntMapper.addDestinationsLinkMapping(1, 3, l1_3);
        Link l3_1 = fac.createLink(Id.createLinkId("3->1"), n3, n1);
        net.addLink(l3_1);
        idIntMapper.addDestinationsLinkMapping(3, 1, l3_1);
        
        Link l3_4 = fac.createLink(Id.createLinkId("3->4"), n3, n4);
        net.addLink(l3_4);
        idIntMapper.addDestinationsLinkMapping(3, 4, l3_4);
        Link l4_3 = fac.createLink(Id.createLinkId("4->3"), n4, n3);
        net.addLink(l4_3);
        idIntMapper.addDestinationsLinkMapping(4, 3, l4_3);

        Link l2_4 = fac.createLink(Id.createLinkId("2->4"), n2, n4);
        net.addLink(l2_4);
        idIntMapper.addDestinationsLinkMapping(2, 4, l2_4);
        Link l4_2 = fac.createLink(Id.createLinkId("4->2"), n4, n2);
        net.addLink(l4_2);
        idIntMapper.addDestinationsLinkMapping(4, 2, l4_2);
        
        Link lout_e = fac.createLink(Id.createLinkId("outEast"), n4, ne);
        net.addLink(lout_e);
        idIntMapper.addDestinationsLinkMapping(4, 5, lout_e);
        Link lin_e = fac.createLink(Id.createLinkId("inEast"), ne, n4);
        net.addLink(lin_e);
        idIntMapper.addDestinationsLinkMapping(5, 4, lin_e);

        Link le_m2 = fac.createLink(Id.createLinkId("destinationEast"), ne, nm2);
        net.addLink(le_m2);
        Link lm2_e = fac.createLink(Id.createLinkId("originEast"), nm2, ne);
        net.addLink(lm2_e);
        

        for (Link l : net.getLinks().values()) {
            l.setFreespeed(1.33);
            l.setCapacity(4 * 1.33);
            l.setNumberOfLanes(4 * 1.33 / 0.71);
            l.setLength(CoordUtils.calcEuclideanDistance(l.getFromNode().getCoord(), l.getToNode().getCoord()));
        }
        Set<String> ext = new HashSet<>();
        ext.add("car");
        ext.add("ext");
        ext.add("2ext");
        lin_w.setAllowedModes(ext);
        lin_e.setAllowedModes(ext);
        Set<String> ext2 = new HashSet<>();
        ext2.add("car");
        ext2.add("ext2");
        lout_e.setAllowedModes(ext2);
        lout_w.setAllowedModes(ext2);

    }
}

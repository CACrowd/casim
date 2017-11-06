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


import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.cacrowd.casim.proto.HybridSimProto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.utils.geometry.CoordUtils;

/***
 * It generates a simulation scenario by importing JSON objects from files.
 * 3 files are needed in the input path: "obstacles.json", "transitions.json" and "network.json" 
 * @author LC
 */
public class JsonScenarioGenerator {
	private static final String path = "src/main/resources";
	
    public static HybridSimProto.Scenario generateScenario(Scenario sc, IdIntMapper mapper) {
        enrichConfig(sc.getConfig());
        createNetwork(sc, mapper);
//        createNetworkChangeEvents(sc);
        createPopulation(sc);
        return createScenario();
    }

    private static HybridSimProto.Scenario createScenario() {
    	HybridSimProto.Scenario.Builder sb = HybridSimProto.Scenario.newBuilder();
    	HybridSimProto.Edge.Builder eb = HybridSimProto.Edge.newBuilder();
    	HybridSimProto.Coordinate.Builder cb = HybridSimProto.Coordinate.newBuilder();
    	
        JSONParser parser = new JSONParser();
        try {
            JSONObject obstacle_objs = (JSONObject) parser.parse(new FileReader(path+"/obstacles.json"));
            for (int i=0; i<obstacle_objs.size();i++){
            	JSONObject obstacle = (JSONObject) obstacle_objs.get(""+i);
            	for (int edge_n=0; edge_n<obstacle.size();edge_n++){
            		JSONObject edge = (JSONObject) obstacle.get("e"+edge_n);
            		JSONArray v0 = (JSONArray)edge.get("v0");
            		JSONArray v1 = (JSONArray)edge.get("v1");
            		
            		cb.setX((int)((Double)v0.get(0)/.05)*.05);
            		cb.setY((int)((Double)v0.get(1)/.05)*.05);
            		eb.setC0(cb.build());
            		cb.setX((int)((Double)v1.get(0)/.05)*.05);
            		cb.setY((int)((Double)v1.get(1)/.05)*.05);
            		eb.setC1(cb.build());
            		eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
            		sb.addEdges(eb.build());
            	}      	
            }           
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            JSONObject transition_objs = (JSONObject) parser.parse(new FileReader(path+"/transitions.json"));
            for (int i=0; i<transition_objs.size();i++){
            	JSONObject transition = (JSONObject) transition_objs.get(""+i);
            	
            	long nodeId = (Long) transition.get("Id");	
        		JSONObject edge = (JSONObject) transition.get("e0");
        		JSONArray v0 = (JSONArray)edge.get("v0");
        		JSONArray v1 = (JSONArray)edge.get("v1");
        		
        		cb.setX((int)((Double)v0.get(0)/.05)*.05);
        		cb.setY((int)((Double)v0.get(1)/.05)*.05);
        		eb.setC0(cb.build());
        		cb.setX((int)((Double)v1.get(0)/.05)*.05);
        		cb.setY((int)((Double)v1.get(1)/.05)*.05);
        		eb.setC1(cb.build());
        		eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        		eb.setId((int) nodeId);
        		sb.addEdges(eb.build());
           	
            }           
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.build();
    }

    private static void enrichConfig(Config c) {
        c.controler().setLastIteration(500);
        c.controler().setWriteEventsInterval(1);
        c.qsim().setEndTime(3600);

        PlanCalcScoreConfigGroup.ActivityParams pre = new PlanCalcScoreConfigGroup.ActivityParams("origin");

        c.strategy().setMaxAgentPlanMemorySize(2);
        c.strategy().addParam("ModuleDisableAfterIteration_1", "500");		//FOR TEST!!!!    "50");
        c.strategy().addParam("Module_1", "ReRoute");
        c.strategy().addParam("ModuleProbability_1", "0.");					//"0.1");
        c.strategy().addParam("Module_2", "ChangeExpBeta");
        c.strategy().addParam("ModuleProbability_2", "1."); 				//"0.9");

        c.travelTimeCalculator().setTravelTimeCalculatorType("TravelTimeCalculatorHashMap");
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
            Activity a0 = fac.createActivityFromLinkId("origin", Id.createLinkId("origin"));
            a0.setEndTime(0);
            plan.addActivity(a0);
            Leg leg = fac.createLeg("car");
            plan.addLeg(leg);
            Activity a1 = fac.createActivityFromLinkId("destination", Id.createLinkId("destination"));
            plan.addActivity(a1);
        }
    }

    private static void createNetwork(Scenario sc, IdIntMapper idIntMapper) {
        Network net = sc.getNetwork();
        net.setCapacityPeriod(1);
        net.setEffectiveLaneWidth(0.71);
        net.setEffectiveCellSize(0.26);
        NetworkFactory fac = net.getFactory();

        Set<String> ext = new HashSet<>();
        ext.add("car");
        ext.add("ext");
        ext.add("2ext");
        Set<String> ext2 = new HashSet<>();
        ext2.add("car");
        ext2.add("ext2");

        JSONParser parser = new JSONParser();
        try {
            JSONObject network_objs = (JSONObject) parser.parse(new FileReader(path+"/network.json"));
            JSONObject node_objs = (JSONObject) network_objs.get("Nodes");
            JSONObject link_objs = (JSONObject) network_objs.get("Links");
            
            
            for (Object key_obj : link_objs.keySet()){
            	String link_id = (String) key_obj;
            	JSONObject link_obj = (JSONObject) link_objs.get(link_id);
            	
            	long fromNodeId = (Long) link_obj.get("n0");
            	Node fromNode = net.getNodes().get(Id.createNodeId(fromNodeId));
        		if (fromNode == null) {
        			JSONArray node_obj = (JSONArray) node_objs.get(""+fromNodeId);
        			Double node_x = (int)((Double)node_obj.get(0)/.05)*.05;
					Double node_y = (int)((Double)node_obj.get(1)/.05)*.05;
					fromNode = fac.createNode(Id.createNodeId(fromNodeId), CoordUtils.createCoord(node_x, node_y));
        			net.addNode(fromNode);        			
        		}
        		long toNodeId = (Long) link_obj.get("n1");
        		Node toNode = net.getNodes().get(Id.createNodeId(toNodeId));
        		if (toNode == null) {
        			JSONArray node_obj = (JSONArray) node_objs.get(""+toNodeId);
        			Double node_x = (int)((Double)node_obj.get(0)/.05)*.05;
					Double node_y = (int)((Double)node_obj.get(1)/.05)*.05;
					toNode = fac.createNode(Id.createNodeId(toNodeId), CoordUtils.createCoord(node_x, node_y));
        			net.addNode(toNode);        			
        		}
            	
        		Link link = fac.createLink(Id.createLinkId(link_id), fromNode, toNode);
                net.addLink(link);
        		if (!(link_id.equals("origin")||link_id.equals("destination"))){
        			idIntMapper.addDestinationsLinkMapping((int)fromNodeId, (int)toNodeId, link);
        		}
        		if (link_id.equals("in")){
        			link.setAllowedModes(ext);
        		}else if(link_id.equals("out")){
        			link.setAllowedModes(ext2);
        		}
        	}       
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (Link l : net.getLinks().values()) {
            l.setFreespeed(1.33);
            l.setCapacity(2 * 1.33);
            l.setNumberOfLanes(2 / 0.71);
            l.setLength(CoordUtils.calcEuclideanDistance(l.getFromNode().getCoord(), l.getToNode().getCoord()));
        }
    }
    
    //simple test
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        System.out.println(0+',');
        try {
            JSONObject transition_objs = (JSONObject) parser.parse(new FileReader(path+"/transitions.json"));
            for (int i=0; i<transition_objs.size();i++){
            	System.out.println(i);
            	
            	JSONObject transition = (JSONObject) transition_objs.get(""+i);
            	
            	Long nodeId = (Long) transition.get("Id");			
            	System.out.println("Id : " + nodeId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

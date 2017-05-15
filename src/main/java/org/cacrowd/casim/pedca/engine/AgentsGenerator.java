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

import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.agents.Population;
import org.cacrowd.casim.pedca.context.Context;

public class AgentsGenerator {

    private static final Logger log = Logger.getLogger(AgentsGenerator.class);

	private Context context;
	private int pedestrianCounter;
	
	public AgentsGenerator(Context context){
		this.context = context;
		pedestrianCounter = 0;
	}

    //Methods never used [GL Aug'16]
//	public void step(){
//		for(Start start : getStarts()){
//			generateFromStart(start);
//		}
//	}
//
//	private void generateFromStart(Start start){
//		int howMany = start.toBeGenerated();
//		ArrayList<GridPoint> usedCells = getPedestrianGrid().getFreePositions(start.getCells());
//		if (howMany>usedCells.size()){
//            log.warn("not enough space in start " + start.toString());
//        }
//		else{
//			usedCells = Lottery.extractObjects(usedCells,howMany);
//		}
//		for(GridPoint p : usedCells){
//			generateSinglePedestrian(p);
//			start.notifyGeneration();
//		}
//	}
//
//	private void generateSinglePedestrian(GridPoint initialPosition) {
//		int pedID = getPopulation().getPedestrians().size();
//		Destination destination = getRandomDestination();
//		Agent pedestrian = new Agent(pedID,initialPosition,destination,context);
//		getPopulation().addPedestrian(pedestrian);
//		context.getPedestrianGrid().addPedestrian(initialPosition, pedestrian);
//	}

//	//FOR MATSIM CONNECTOR
//	public Pedestrian generatePedestrian(GridPoint initialPosition, int destinationId, QVehicle vehicle, TransitionArea transitionArea){
//		int pedID = pedestrianCounter;
//		Destination destination = context.getMarkerConfiguration().getDestination(destinationId);
//
//		//TODO FIXME this looks evil. First an Agent is instantiated, then it is passed as an Argument to Pedestrian, which in
//		//turn extends Agent [gl Aug '16]
//		Agent agent = new Agent(pedID,initialPosition,destination,context);
//		Pedestrian pedestrian = new Pedestrian(agent, vehicle, transitionArea);
//
//		getPopulation().addPedestrian(pedestrian);
//		//context.getPedestrianGrid().addPedestrian(initialPosition, pedestrian);
//		pedestrianCounter++;
//		return pedestrian;
//
//	}
//
//	//FOR MATSIM CONNECTOR
//	public Context getContext(){
//		return context;
//	}

    //Method never used
//	//FOR MATSIM CONNECTOR
//	public GridPoint getFreePosition(int destinationId){
//		ArrayList<GridPoint> cells = getContext().getMarkerConfiguration().getDestination(destinationId).getCells();
// 		ArrayList<GridPoint> usedCells = getPedestrianGrid().getFreePositions(cells);
// 		return Lottery.extractObjects(usedCells,1).get(0);
//	}

    //MEthod never used
//	private Destination getRandomDestination() {
//		return Lottery.extractObject(context.getMarkerConfiguration().getDestinations());
//	}

    private Population getPopulation(){
		return context.getPopulation();
	}

    //MEthod never used
//	private ArrayList<Start> getStarts(){
//		return context.getMarkerConfiguration().getStarts();
//	}

    //Method never used
//	private PedestrianGrid getPedestrianGrid(){
//		return context.getPedestrianGrid();
//	}
}

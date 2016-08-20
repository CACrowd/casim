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

import org.cacrowd.casim.pedca.context.Context;

public class SimulationEngine {
	private AgentsGenerator agentGenerator;
	private AgentsUpdater agentUpdater;
	private ConflictSolver conflictSolver;
	private AgentMover agentMover;
	private GridsAndObjectsUpdater activeObjectsUpdater;
	
	public SimulationEngine(Context context){
		agentGenerator = new AgentsGenerator(context);
		agentUpdater = new AgentsUpdater(context.getPopulation());
		conflictSolver = new ConflictSolver(context);
		agentMover = null;	//must be set with setter method
        activeObjectsUpdater = new GridsAndObjectsUpdater(context);
	}

    //FOR MATSIM CONNECTOR
	public void doSimStep(double time){
		//Log.log("STEP at: "+time);
		agentUpdater.step(time);
		conflictSolver.step();
		agentMover.step(time);		
		activeObjectsUpdater.step(time);
	}
	
	//FOR MATSIM CONNECTOR
	public AgentsGenerator getAgentGenerator(){
		return agentGenerator;
	}
	
	//FOR MATSIM CONNECTOR
	public void setAgentMover(AgentMover agentMover){
		this.agentMover = agentMover;
	}

}

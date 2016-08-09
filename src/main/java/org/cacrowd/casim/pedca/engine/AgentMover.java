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
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.agents.Population;
import org.cacrowd.casim.pedca.context.Context;

public class AgentMover {

    private static final Logger log = Logger.getLogger(AgentMover.class);

    //private final Context context;
    private final Population population;
	
	public AgentMover(Context context){
		//this.context = context;
		this.population = context.getPopulation();
	}
	
	public void step(){
		for(int index=0; index<population.size(); index++){
			Agent pedestrian = population.getPedestrian(index);
			if (pedestrian.isArrived()){
                log.info(pedestrian.toString() + " exited.");
                moveToUniverse(pedestrian);
				index--;
			}
			else{
				pedestrian.move();
			}
		}
	}
	
	//FOR MATSIM CONNECTOR
	public void step(double time){
		throw new RuntimeException("this method is not implemented here");
	}

	private void moveToUniverse(Agent pedestrian) {
		pedestrian.leavePedestrianGrid();
		population.remove(pedestrian);
	}
	
	protected Population getPopulation(){
		return population;
	}
}

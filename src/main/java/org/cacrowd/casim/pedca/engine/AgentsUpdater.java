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

import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.agents.Population;

public class AgentsUpdater {
	private Population population;
	
	public AgentsUpdater(Population population){
		this.population = population;
	}
	
	public void step(double time){
		if (!population.isEmpty()){
			for(Agent pedestrian : population.getPedestrians()){
				//TODO maybe here is not the right place for this check 
				if (!pedestrian.isArrived()){
					pedestrian.updateChoice(time);
				}
			}
		}
	}
}

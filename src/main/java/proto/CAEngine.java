package proto;
/****************************************************************************/
// casim, cellular automaton simulation for multi-destination pedestrian
// crowds; see https://github.com/CACrowd/casim
// Copyright (C) 2016 CACrowd and contributors
/****************************************************************************/
//
//   This file is part of casim.
//   casim is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 2 of the License, or
//   (at your option) any later version.
//
/****************************************************************************/

import matsimconnector.scenario.CAEnvironment;
import matsimconnector.utility.Constants;
import org.matsim.api.core.v01.Id;
import pedca.engine.SimulationEngine;

import java.util.Map;

/**
 * Created by laemmel on 05/05/16.
 */
public class CAEngine {

	private Map<Id<CAEnvironment>, SimulationEngine> enginesCA;
	private double simCATime;

	public void doSimStep(double time) {
		double stepDuration = Constants.CA_STEP_DURATION;
		//Log.log("------> BEGINNING STEPS AT "+time);
		for (; this.simCATime < time; this.simCATime += stepDuration) {
			for (SimulationEngine engine : this.enginesCA.values()) {
				double currentTime = System.currentTimeMillis();
				engine.doSimStep(this.simCATime);
				double afterTime = System.currentTimeMillis();
//				qSim.getEventsManager().processEvent(new CAEngineStepPerformedEvent(this.simCATime, (float)(afterTime-currentTime), engine.getAgentGenerator().getContext().getPopulation().size()));
			}
		}
	}

}

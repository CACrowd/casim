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

package org.cacrowd.casim.matsimconnector.run;

import org.cacrowd.casim.matsimconnector.scenariogenerator.ScenarioGenerator;
import org.cacrowd.casim.matsimconnector.utility.Constants;

public class LoadAndRunCASimulation {

	public static void main(String[] args) {
		if(args.length != 0 && Boolean.parseBoolean(args[0])){
			Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = Boolean.parseBoolean(args[0]);
			Constants.OUTPUT_PATH = Constants.DEBUG_TEST_PATH+"/outputSO";
			Constants.INPUT_PATH = Constants.DEBUG_TEST_PATH+"/inputSO";
		}else{
			Constants.OUTPUT_PATH = Constants.DEBUG_TEST_PATH+"/outputNE";
			Constants.INPUT_PATH = Constants.DEBUG_TEST_PATH+"/inputNE";
		}		
				
		ScenarioGenerator.main(new String[0]);
		CASimulationRunner.main(new String[0]);
	}	

}

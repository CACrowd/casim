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

import org.cacrowd.casim.matsimconnector.utility.Constants;

public class RunDaganzoExperiments {
	public static void main(String [] args) {
		Constants.VIS = true;
		Constants.ORIGIN_FLOWS = new String[1];
		Constants.ORIGIN_FLOWS[0] = "e";

		Constants.ENVIRONMENT_FILE = "environmentGrid_Dag04.csv";
		Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = true;
//		Constants.OUTPUT_PATH = "/Users/laemmel/devel/CACAIE/daganzo_04_so/";
		
		LoadAndRunCASimulation.main(new String[]{});
		
		Constants.ENVIRONMENT_FILE = "environmentGrid_Dag08.csv";
		Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = true;
//		Constants.OUTPUT_PATH = "/Users/laemmel/devel/CACAIE/daganzo_08_so/";
		LoadAndRunCASimulation.main(new String[]{});
		
		Constants.ENVIRONMENT_FILE = "environmentGrid_Dag12.csv";
		Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = true;
//		Constants.OUTPUT_PATH = "/Users/laemmel/devel/CACAIE/daganzo_12_so/";
		LoadAndRunCASimulation.main(new String[]{});

		Constants.ENVIRONMENT_FILE = "environmentGrid_Dag04.csv";
		Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = false;
//		Constants.OUTPUT_PATH = "/Users/laemmel/devel/CACAIE/daganzo_04_ne/";
		LoadAndRunCASimulation.main(new String[]{});
		
		Constants.ENVIRONMENT_FILE = "environmentGrid_Dag08.csv";
		Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = false;
//		Constants.OUTPUT_PATH = "/Users/laemmel/devel/CACAIE/daganzo_08_ne/";
		LoadAndRunCASimulation.main(new String[]{});
		
		Constants.ENVIRONMENT_FILE = "environmentGrid_Dag12.csv";
		Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = false;
//		Constants.OUTPUT_PATH = "/Users/laemmel/devel/CACAIE/daganzo_12_ne/";
		LoadAndRunCASimulation.main(new String[]{});

	}
}

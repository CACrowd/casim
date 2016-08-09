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

package org.cacrowd.casim.scenarios;

import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.SimulationEngine;

public class Controller {

	public static void main(String[] args) {
		Context context = ContextGenerator.getCorridorContext(8, 25, 106);
		SimulationEngine engine = new SimulationEngine(100,context);
		engine.run();
	}

}

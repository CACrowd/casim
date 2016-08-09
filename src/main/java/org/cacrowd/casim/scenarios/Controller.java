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

package scenarios;

import pedca.context.Context;
import pedca.engine.SimulationEngine;

public class Controller {

	public static void main(String[] args) {
		Context context = ContextGenerator.getCorridorContext(8, 25, 106);
		SimulationEngine engine = new SimulationEngine(100,context);
		engine.run();
	}

}

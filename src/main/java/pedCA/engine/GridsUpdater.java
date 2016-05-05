package pedca.engine;

import pedca.context.Context;
import pedca.environment.grid.ActiveGrid;
import pedca.environment.grid.PedestrianGrid;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class GridsUpdater {
	private ArrayList<ActiveGrid> activeGrids;
	
	public GridsUpdater(Context context) {
		this.activeGrids = new ArrayList<ActiveGrid>();
		for (PedestrianGrid pedestrianGrid : context.getPedestrianGrids())
			activeGrids.add(pedestrianGrid);
	}

	public void step(){
		for (ActiveGrid activeGrid : activeGrids){
			activeGrid.update();
		}
	}
	
}

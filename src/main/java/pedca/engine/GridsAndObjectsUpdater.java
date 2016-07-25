package pedca.engine;

import java.util.ArrayList;

import pedca.context.Context;
import pedca.environment.grid.ActiveGrid;
import pedca.environment.grid.PedestrianGrid;
import pedca.environment.markers.ConstrainedFlowDestination;
import pedca.environment.markers.Destination;

@SuppressWarnings("rawtypes")
public class GridsAndObjectsUpdater {
	private ArrayList<ActiveGrid> activeGrids;
	private ArrayList<ConstrainedFlowDestination> activeDestinations;
	
	public GridsAndObjectsUpdater(Context context) {
		this.activeGrids = new ArrayList<ActiveGrid>();
		this.activeDestinations = new ArrayList<ConstrainedFlowDestination>();
		for (PedestrianGrid pedestrianGrid : context.getPedestrianGrids())
			activeGrids.add(pedestrianGrid);
		for (Destination dest : context.getMarkerConfiguration().getDestinations())
			if (dest instanceof ConstrainedFlowDestination)
				activeDestinations.add((ConstrainedFlowDestination)dest);
	}

	public void step(){
		for (ActiveGrid activeGrid : activeGrids){
			activeGrid.step();
		}
		for (ConstrainedFlowDestination dest : activeDestinations)
			dest.step();
	}
	
}

package pedca.engine;

import java.util.ArrayList;

import pedca.context.Context;
import pedca.environment.grid.ActiveGrid;
import pedca.environment.grid.PedestrianGrid;
import pedca.environment.markers.ConstrainedFlowDestination;
import pedca.environment.markers.DelayedDestination;
import pedca.environment.markers.Destination;

@SuppressWarnings("rawtypes")
public class GridsAndObjectsUpdater {
	private ArrayList<ActiveGrid> activeGrids;
	private ArrayList<DelayedDestination> activeDestinations;
	
	public GridsAndObjectsUpdater(Context context) {
		this.activeGrids = new ArrayList<ActiveGrid>();
		this.activeDestinations = new ArrayList<DelayedDestination>();
		for (PedestrianGrid pedestrianGrid : context.getPedestrianGrids())
			activeGrids.add(pedestrianGrid);
		for (Destination dest : context.getMarkerConfiguration().getDestinations())
			if (dest instanceof DelayedDestination)
				activeDestinations.add((DelayedDestination)dest);
	}

	public void step(double time){
		for (ActiveGrid activeGrid : activeGrids){
			activeGrid.step();
		}
		for (DelayedDestination dest : activeDestinations)
			dest.step(time);
	}
	
}

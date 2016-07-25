package pedca.engine;

import java.util.ArrayList;

import pedca.context.Context;
import pedca.environment.grid.ActiveGrid;
import pedca.environment.grid.PedestrianGrid;
import pedca.environment.markers.Destination;
import pedca.environment.markers.TacticalDestination;

@SuppressWarnings("rawtypes")
public class GridsAndObjectsUpdater {
	private ArrayList<ActiveGrid> activeGrids;
	private ArrayList<TacticalDestination> activeDestinations;
	
	public GridsAndObjectsUpdater(Context context) {
		this.activeGrids = new ArrayList<ActiveGrid>();
		this.activeDestinations = new ArrayList<TacticalDestination>();
		for (PedestrianGrid pedestrianGrid : context.getPedestrianGrids())
			activeGrids.add(pedestrianGrid);
		for (Destination dest : context.getMarkerConfiguration().getDestinations())
			if (dest instanceof TacticalDestination && ((TacticalDestination)dest).hasDelayToCross())
				activeDestinations.add((TacticalDestination)dest);
	}

	public void step(){
		for (ActiveGrid activeGrid : activeGrids){
			activeGrid.step();
		}
		for (TacticalDestination dest : activeDestinations)
			dest.step();
	}
	
}

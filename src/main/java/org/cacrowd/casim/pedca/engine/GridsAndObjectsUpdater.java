package org.cacrowd.casim.pedca.engine;

import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.ActiveGrid;
import org.cacrowd.casim.pedca.environment.grid.PedestrianGrid;
import org.cacrowd.casim.pedca.environment.markers.DelayedDestination;
import org.cacrowd.casim.pedca.environment.markers.Destination;

import java.util.ArrayList;

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

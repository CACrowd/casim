package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;

import java.util.ArrayList;

public class DelayedDestination extends TacticalDestination {
	
	private static final long serialVersionUID = 1L;
	private final int stepToCross;

	public DelayedDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, int stepToCross) {
		super(coordinate, cells, isStairsBorder);
		this.stepToCross = stepToCross;
	}

	/**
	 * returns the time (in steps) needed by pedestrians to cross the destination, to design doors or turnstiles. 
	 * @Input simulation time
	 */
	public int waitingTimeForCrossing(double time){
		return stepToCross;
	}
	
	public void step(double time){
		//DO NOTHING
	}
}

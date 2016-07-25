package pedca.environment.markers;

import java.util.ArrayList;

import pedca.environment.grid.GridPoint;
import pedca.environment.network.Coordinate;

public class DelayedDestination extends TacticalDestination {
	
	private static final long serialVersionUID = 1L;
	private final int stepToCross;

	public DelayedDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, int stepToCross) {
		super(coordinate, cells, isStairsBorder);
		this.stepToCross = stepToCross;
	}

	/**
	 * returns the time (in steps) needed by pedestrians to cross the destination, to design doors or turnstiles. 
	 */
	public int waitingTimeForCrossing(){
		return stepToCross;
	}	
}

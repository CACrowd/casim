package pedca.environment.markers;

import java.util.ArrayList;

import pedca.environment.grid.GridPoint;
import pedca.environment.network.Coordinate;
import pedca.utility.Constants;

public class ConstrainedFlowDestination extends DelayedDestination {
	private static final long serialVersionUID = 1L;
	
	//to model turnstiles or other doors implying delays to walk through - if 0 means that the object is just a set of normal walkable cells
	private final float stepToCross;
	private float currentStepToCross;
	
	public ConstrainedFlowDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, float flowCapacity) {
		super(coordinate, cells, isStairsBorder,0);
		if (flowCapacity == 0)
			this.stepToCross = 0;
		else
			this.stepToCross = (float) ((1 / flowCapacity) / Constants.STEP_DURATION);
		currentStepToCross = 0;
	}
	
	@Override
	public int waitingTimeForCrossing(double time){
		int result = (int)currentStepToCross;
		currentStepToCross+=stepToCross;
		return result;
	}

	@Override
	public void step(double time) {
		if (currentStepToCross > 0)
			--currentStepToCross;		
	}

}

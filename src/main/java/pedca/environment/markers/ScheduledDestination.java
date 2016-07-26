package pedca.environment.markers;

import java.util.ArrayList;

import matsimconnector.utility.Constants;
import pedca.environment.grid.GridPoint;
import pedca.environment.network.Coordinate;

public class ScheduledDestination extends DelayedDestination {

	private static final long serialVersionUID = 1L;

	private double[] scheduledTimes;
	private int scheduleIndex;
	private int timeShift;
	
	public ScheduledDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, double[] scheduleTimes) {
		super(coordinate, cells, isStairsBorder, 0);
		this.scheduledTimes = scheduleTimes;
		this.scheduleIndex = 0;
		this.timeShift = 240;
	}

	@Override
	public int waitingTimeForCrossing(double time){
		int dayTime = (int)time % 86400;
		int stepToCross = (int)((scheduledTimes[scheduleIndex] - dayTime + timeShift)/Constants.CA_STEP_DURATION);
		if (stepToCross > 0)
			return stepToCross;
		return 0;
	}
	
	@Override
	public void step(double time) {
		int dayTime = (int)time % 86400;
		if (dayTime > scheduledTimes[scheduleIndex]+2*timeShift)
			scheduleIndex=(scheduleIndex+1)%scheduledTimes.length;
	}
	
}

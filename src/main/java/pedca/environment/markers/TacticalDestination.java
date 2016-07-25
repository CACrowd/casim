package pedca.environment.markers;

import pedca.environment.grid.GridPoint;
import pedca.environment.network.Coordinate;
import pedca.utility.Constants;

import java.util.ArrayList;

public class TacticalDestination extends Destination {

	private static final long serialVersionUID = 1L;
	private boolean isStairsBorder;
	private double width;
	private final Coordinate coordinate;
	
	//to model turnstiles or other doors implying delays to walk through - if 0 means that the object is just a set of normal walkable cells
	private final float stepToCross;
	private float currentStepToCross;
	

	public TacticalDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder) {
		this(coordinate, cells, isStairsBorder, 0);
	}
	
	public TacticalDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, float flowCapacity){
		super(cells);
		this.coordinate = coordinate;
		this.isStairsBorder = isStairsBorder;
		if (flowCapacity == 0)
			this.stepToCross = 0;
		else
			this.stepToCross = (1/flowCapacity)/Constants.STEP_DURATION;
		currentStepToCross = 0;
		calculateWidth();
	}

	public boolean isStairsBorder() {
		return isStairsBorder;
	}

	/**
	 * returns the time (in steps) needed by pedestrians to cross the destination, to design doors or turnstiles. 
	 */
	public int waitingTimeForCrossing(){
		int result = (int)currentStepToCross;
		currentStepToCross+=stepToCross;
		return result;
	}
	
	public boolean hasDelayToCross() {
		return stepToCross>0;
	}

	public void step() {
		if (currentStepToCross > 0)
			--currentStepToCross;		
	}

	/**
	 * TODO: till now the width is calculated by only considering
	 * cases where the destination represent a perfectly horizontal 
	 * or vertical set of cells
	 * */
	private void calculateWidth(){
		width = cells.size()*Constants.CELL_SIZE;
	}

	public double getWidth() {
		return width;
	}
	
	public int getID(){
		return getLevel();
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}


	
}

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
	

	public TacticalDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder) {
		super(cells);
		this.coordinate = coordinate;
		this.isStairsBorder = isStairsBorder;
		calculateWidth();
	}

	public boolean isStairsBorder() {
		return isStairsBorder;
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

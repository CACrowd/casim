package org.cacrowd.casim.environment;

import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.utility.MathUtility;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.PedestrianGrid;
import org.cacrowd.casim.pedca.environment.markers.FinalDestination;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.pedca.utility.Lottery;

import java.util.ArrayList;

public class TransitionArea extends PedestrianGrid {

	private int rotation;
	private GridPoint environmentRef;	//coordinates of the top cell of the transition Area in the real grid
	private GridPoint transAreaRef;
	private FinalDestination destinationRef;
	private ArrayList<GridPoint> positionsForGeneration;
	
	public TransitionArea(int rows, int cols, FinalDestination destination){
		this(rows, cols, destination.getRotation(),destination.getEnvironmentRef());
		this.destinationRef = destination;
	}
	
	public TransitionArea(int rows, int cols, GridPoint realEnvironmentRef) {
		this(rows,cols,0, realEnvironmentRef);
	}
	
	public TransitionArea(int rows, int cols, int rotation, GridPoint environmentRef) {
		super(rows,cols, null);
		this.rotation = rotation;
		this.environmentRef = environmentRef;
		this.transAreaRef = new GridPoint(cols-1,0);
		MathUtility.rotate(transAreaRef, this.rotation);
		calculatePositionsForGeneration();
	}
	
	//TODO OPTIMIZE THIS
	public boolean acceptPedestrians(){
		return getFreePositions(positionsForGeneration).size()>2;
	}
	
	public boolean isAtBorder(Pedestrian pedestrian){
		GridPoint position = pedestrian.getPosition();
		return isAtBorder(position) && this.get(position).contains(pedestrian);
	}
	
	public boolean isAtBorder(GridPoint position){
		return position.getX() == getColumns()-1;
	}
	
	//TODO TEST
	public GridPoint convertTAPosToEnvPos(GridPoint tAPosition){
		GridPoint result = new GridPoint(tAPosition.getX(),tAPosition.getY());
		MathUtility.rotate(result, rotation);
		int x_r = result.getX()-transAreaRef.getX()+environmentRef.getX();
		int y_r = result.getY()-transAreaRef.getY()+environmentRef.getY();
		result.setX(x_r); result.setY(y_r);
		return result;
	}
	
	public GridPoint convertEnvPosToTAPos(GridPoint envPosition){
		GridPoint result = new GridPoint(envPosition.getX(),envPosition.getY());
		int x_ta = result.getX()+transAreaRef.getX()-environmentRef.getX();
		int y_ta = result.getY()+transAreaRef.getY()-environmentRef.getY();
		result.setX(x_ta); result.setY(y_ta);
		MathUtility.rotate(result, 360-rotation);
		return result;
	}

	public Coordinate convertCoordinates(Coordinate tACoordinate) {
		Coordinate result = new Coordinate(tACoordinate.getX(), tACoordinate.getY());
		Coordinate shift = new Coordinate(0, 0);
		MathUtility.rotate(result, rotation);
		MathUtility.rotate(shift, rotation, Constants.CA_CELL_SIDE*0.5, Constants.CA_CELL_SIDE*0.5);
		Coordinate transAreaRef = gridPoint2Coordinate(this.transAreaRef);
		Coordinate environmentRef = gridPoint2Coordinate(this.environmentRef);
		double x_r = result.getX()-transAreaRef.getX()+environmentRef.getX()+shift.getX();
		double y_r = result.getY()-transAreaRef.getY()+environmentRef.getY()+shift.getY();
		result.setX(x_r); result.setY(y_r);
		return result;
	}

	public GridPoint calculateEnterPosition(){
		GridPoint result = Lottery.extractObject(getFreePositions(positionsForGeneration));
		return result;
	}

	public double getSFFValue(GridPoint position, boolean destinationReached) {
		if (destinationReached)
			return position.getX();
		else
			return getColumns()-1-position.getX();
	}
	
	public FinalDestination getReferenceDestination(){
		return destinationRef;
	}
	
	/**
	 * returns all free positions useful for the generation.
	 * The last column, which is overimposed with the environment, is not considered.
	 * */
	private ArrayList<GridPoint> calculatePositionsForGeneration(){
		positionsForGeneration = new ArrayList<GridPoint>();
		for (int i=0;i<cells.size();i++)
			for (int j=0;j<cells.get(i).size()-1;j++)
				positionsForGeneration.add(new GridPoint(j,i));
		return positionsForGeneration;
	}
}

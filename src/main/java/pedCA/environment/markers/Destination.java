package pedCA.environment.markers;

import pedCA.environment.grid.GridPoint;

import java.util.ArrayList;

public class Destination extends Marker{

	private static final long serialVersionUID = 1L;
	private int level;
	
	public Destination(ArrayList<GridPoint> cells){
		super(cells);
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public int getLevel(){
		return level;
	}
}

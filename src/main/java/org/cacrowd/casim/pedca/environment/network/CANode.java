package org.cacrowd.casim.pedca.environment.network;

public class CANode {
	private int id;
	private int destinationId;
	private Coordinate coordinate;
	private double width;


	public CANode(int id, Coordinate coordinate, double width) {
		this.id = id;
		this.coordinate = coordinate;
		this.width = width;
		setDestinationId(id);
	}

	public int getId() {
		return id;
	}
	
	public int getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(int destinationId) {
		this.destinationId = destinationId;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}
	
	public double getWidth() {
		return width;
	}
	
	public String toString(){
		String result = "Coordinate: " + coordinate.toString() + "\n";
		result += "WIDTH: "+width;
		return result;
	}
	
	
}

package pedca.environment.network;

import matsimconnector.utility.Constants;
import pedca.environment.grid.GridPoint;

import java.io.Serializable;

public class Coordinate implements Serializable {

    private static final long serialVersionUID = 1L;
    private double x;
    private double y;

//	public Coordinate(GridPoint gp){
//		this.x=gp.getX()*Constants.CA_CELL_SIDE;
//		this.y=gp.getY()*Constants.CA_CELL_SIDE;
//	}

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

}

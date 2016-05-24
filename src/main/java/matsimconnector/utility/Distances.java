package matsimconnector.utility;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import pedca.environment.grid.GridPoint;
import pedca.environment.network.Coordinate;

import java.util.ArrayList;

public class Distances {

	public static Double EuclideanDistance(GridPoint gp1, GridPoint gp2) {
		return Math.sqrt((gp1.getX()-gp2.getX())^2+(gp1.getY()-gp2.getY())^2);
	}

	public static double EuclideanDistance(Coordinate c1, Coordinate c2) {
		return Math.sqrt(Math.pow(c1.getX()-c2.getX(),2)+Math.pow(c1.getY()-c2.getY(),2));
	}

	public static double EuclideanDistance(Link l) {
		Coord c1 = l.getFromNode().getCoord();
		Coord c2 = l.getToNode().getCoord();
		return EuclideanDistance(c1, c2);
	}

	protected static double EuclideanDistance(Coord c1, Coord c2) {
		return Math.sqrt(Math.pow(c1.getX()-c2.getX(),2)+Math.pow(c1.getY()-c2.getY(),2));
	}

	public static Coordinate centroid(ArrayList<Node> nodes) {
		Coordinate result = new Coordinate(0, 0);
		for (Node node : nodes){
			result.setX(result.getX()+node.getCoord().getX());
			result.setY(result.getY()+node.getCoord().getY());
		}
		result.setX(result.getX()/nodes.size());
		result.setY(result.getY()/nodes.size());
		return result;
	}
	
}

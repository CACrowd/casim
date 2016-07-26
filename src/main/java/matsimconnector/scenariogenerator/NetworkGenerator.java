package matsimconnector.scenariogenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import matsimconnector.utility.Constants;
import matsimconnector.utility.Distances;
import matsimconnector.utility.MathUtility;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkImpl;

import pedca.context.Context;
import pedca.environment.markers.Destination;
import pedca.environment.markers.FinalDestination;
import pedca.environment.network.Coordinate;

public class NetworkGenerator {

	private static final Double DOOR_WIDTH = Constants.FAKE_LINK_WIDTH;
	/*package*/ static double LINK_LENGTH = 10.;
	private static double FLOW = Constants.FLOPW_CAP_PER_METER_WIDTH * DOOR_WIDTH;
	private static Set<String> MODES = new HashSet<String>();
	static{
		MODES.add("walk"); //MODES.add("car");
	}
	private static int nodeCount = 0;
	private static int linkCount = 0;
	
	public static void createNetwork(Scenario sc, Context contextCA) {
		Network net = sc.getNetwork();
		NetworkFactory fac = net.getFactory();
		int nLinks = 2;
		
		ArrayList<Node> south = new ArrayList<Node>();
		ArrayList<Node> north = new ArrayList<Node>();
		ArrayList<Node> east = new ArrayList<Node>();
		ArrayList<Node> west = new ArrayList<Node>();
		
		/////////TO LINK MULTIPLE SPACES
		double[] nodeShift = {contextCA.environmentOrigin.getX(), contextCA.environmentOrigin.getY()};
		
		for (Destination dest : contextCA.getMarkerConfiguration().getDestinations()){
			if (dest instanceof FinalDestination){
				FinalDestination destinationCA = (FinalDestination) dest;
				ArrayList<Node> nodes = new ArrayList<Node>();
				for (int i=0; i<nLinks;i++){
					Coordinate refCoord = new Coordinate(destinationCA.getCoordinate().getX() + nodeShift[0], destinationCA.getCoordinate().getY() + nodeShift[1]);
					Coordinate newCoord = new Coordinate(refCoord.getX() - (LINK_LENGTH * i) - 0.2, refCoord.getY());
					int rotation = destinationCA.getRotation();
					MathUtility.rotate(newCoord, rotation, refCoord);
					MathUtility.rotate(newCoord, contextCA.environmentRotation, nodeShift[0], nodeShift[1]);
					Node node = fac.createNode(Id.create("n"+nextNodeId(),Node.class), new Coord(newCoord.getX(),newCoord.getY()));
					nodes.add(node);
					net.addNode(node);
					
					if (i==nLinks-1){
						if (rotation == 0)
							west.add(node);
						if (rotation == 90)
							south.add(node);
						if (rotation == 180)
							east.add(node);
						if (rotation == 270)
							north.add(node);
					}
				}			
				for (int i=1; i<nLinks;i++){
					Link linkOut = fac.createLink(Id.create("l"+nextLinkId(),Link.class), nodes.get(i-1), nodes.get(i));
					Link linkIn = fac.createLink(Id.create("l"+nextLinkId(),Link.class), nodes.get(i), nodes.get(i-1));
					initLink(linkOut);
					initLink(linkIn);
					net.addLink(linkOut);
					net.addLink(linkIn);
				}
			}
		}
		/**
		Double x_min=null;
		Double y_min=null;
		Double x_max=null;
		Double y_max=null;
		boolean firstIt = true;		
		for (Node node : net.getNodes().values()){
			if (firstIt){
				x_min = node.getCoord().getX();
				x_max = node.getCoord().getX();
				y_min = node.getCoord().getY();
				y_max = node.getCoord().getY();
				firstIt = false;
			}
			else{
				if(node.getCoord().getY() < y_min)
					y_min = node.getCoord().getY();
				if(node.getCoord().getY() > y_max)
					y_max = node.getCoord().getY();
				if(node.getCoord().getX() < x_min)
					x_min = node.getCoord().getX();
				if(node.getCoord().getX() > x_max)
					x_max = node.getCoord().getX();
			}
		}
		for (Node node : net.getNodes().values()){
			if (node.getCoord().getY() == y_min && y_min < 0)
				south.add(node);
			if (node.getCoord().getY() == y_max && y_max > (double)contextCA.getRows()*Constants.CA_CELL_SIDE)
				north.add(node);
			if (node.getCoord().getX() == x_min && x_min < 0)
				west.add(node);
			if (node.getCoord().getX() == x_max && x_max > (double)contextCA.getColumns()*Constants.CA_CELL_SIDE)
				east.add(node);
		}
		**/
		
		if (south.size()>0){
			Coordinate centroid = Distances.centroid(south);
			Node orDestNode = fac.createNode(Id.create("n"+nextNodeId(),Node.class), new Coord(centroid.getX(),centroid.getY()-LINK_LENGTH));
			net.addNode(orDestNode);
			connect(orDestNode, south, net, fac,'s', nodeShift);
		
		}
		if (north.size()>0){
			Coordinate centroid = Distances.centroid(north);
			Node orDestNode = fac.createNode(Id.create("n"+nextNodeId(),Node.class), new Coord(centroid.getX(),centroid.getY()+LINK_LENGTH));
			net.addNode(orDestNode);
			connect(orDestNode, north, net, fac,'n', nodeShift);
		}
		if (west.size()>0){
			Coordinate centroid = Distances.centroid(west);
			Node orDestNode = fac.createNode(Id.create("n"+nextNodeId(),Node.class), new Coord(centroid.getX()-LINK_LENGTH,centroid.getY()));
			net.addNode(orDestNode);
			connect(orDestNode, west, net, fac, 'w', nodeShift);
		}
		if (east.size()>0){
			Coordinate centroid = Distances.centroid(east);
			Node orDestNode = fac.createNode(Id.create("n"+nextNodeId(),Node.class), new Coord(centroid.getX()+LINK_LENGTH,centroid.getY()));
			net.addNode(orDestNode);
			connect(orDestNode, east, net, fac, 'e', nodeShift);
		}
		((NetworkImpl)net).setCapacityPeriod(1);
		((NetworkImpl)net).setEffectiveCellSize(.26);
		((NetworkImpl)net).setEffectiveLaneWidth(.71);		
	}

	private static void connect(Node orDestNode, ArrayList<Node> nodes, Network net, NetworkFactory fac, char direction, double[] nodeShift) {
		for (Node node : nodes){
			Link linkOut = fac.createLink(Id.create("l"+nextLinkId(),Link.class), node, orDestNode);
			Link linkIn = fac.createLink(Id.create("l"+nextLinkId(),Link.class), orDestNode, node);
			initLink(linkOut);
			initLink(linkIn);
			net.addLink(linkOut);
			net.addLink(linkIn);
		}
		if ((nodeShift[0] == 0 && nodeShift[1] == 0) || direction != 's'){
			Node firstNode;
			if (direction == 'n' && (nodeShift[0] == 0 && nodeShift[1] == 0))
				firstNode = fac.createNode(Id.create("n_"+direction,Node.class), new Coord(orDestNode.getCoord().getX(),orDestNode.getCoord().getY()+LINK_LENGTH));
			else if (direction == 'n')
				firstNode = fac.createNode(Id.create("n_2"+direction,Node.class), new Coord(orDestNode.getCoord().getX(),orDestNode.getCoord().getY()+LINK_LENGTH));
			else
				firstNode = fac.createNode(Id.create("n_"+direction,Node.class), new Coord(orDestNode.getCoord().getX(),orDestNode.getCoord().getY()+LINK_LENGTH));
			Link linkOut = fac.createLink(Id.create("l"+nextLinkId(),Link.class), orDestNode, firstNode);
			Link linkIn = fac.createLink(Id.create("l"+nextLinkId(),Link.class), firstNode, orDestNode);
			net.addNode(firstNode);
			initLink(linkOut);
			initLink(linkIn);
			net.addLink(linkOut);
			net.addLink(linkIn);
		}else{
				Node prevDestNode = net.getNodes().get(Id.create("n_"+'s',Node.class));
				Link inLink = prevDestNode.getInLinks().values().iterator().next();
				Link outLink = prevDestNode.getOutLinks().values().iterator().next();
				net.removeLink(inLink.getId());
				net.removeLink(outLink.getId());
				net.removeNode(prevDestNode.getId());
				Node firstNode = inLink.getFromNode();
				Link linkOut = fac.createLink(Id.create("l"+nextLinkId(),Link.class), orDestNode, firstNode);
				Link linkIn = fac.createLink(Id.create("l"+nextLinkId(),Link.class), firstNode, orDestNode);
				initLink(linkOut);
				initLink(linkIn);
				net.addLink(linkOut);
				net.addLink(linkIn);
		}
	}

	/*package*/ static void initLink(Link link) {
		link.setLength(MathUtility.EuclideanDistance(link.getFromNode().getCoord(), link.getToNode().getCoord())+.000001);
		link.setAllowedModes(MODES);
		link.setFreespeed(Constants.PEDESTRIAN_SPEED);
		link.setCapacity(FLOW);
		//link.setNumberOfLanes(LANES);
	}
	
	private static int nextNodeId(){
		return nodeCount++;
	}
	
	private static int nextLinkId(){
		return linkCount++;
	}
	
}

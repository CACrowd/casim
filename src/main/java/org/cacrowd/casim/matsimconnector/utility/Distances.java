/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.matsimconnector.utility;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;

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

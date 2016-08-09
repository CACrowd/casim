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

package org.cacrowd.casim.pedca.utility;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;

public class Distances {

	public static Double EuclideanDistance(GridPoint gp1, GridPoint gp2) {
		return Math.sqrt((gp1.getX()-gp2.getX())^2+(gp1.getY()-gp2.getY())^2);
	}

	public static double EuclideanDistance(Coordinate c1, Coordinate c2) {
		return Math.sqrt(Math.pow(c1.getX()-c2.getX(),2)+Math.pow(c1.getY()-c2.getY(),2));
	}

	public static GridPoint gridPointDifference(GridPoint gp1, GridPoint gp2){
		return new GridPoint(gp1.getX()-gp2.getX(), gp1.getY()-gp2.getY());
	}
	
	public static GridPoint gridPointSum(GridPoint gp1, GridPoint gp2){
		return new GridPoint(gp1.getX()+gp2.getX(), gp1.getY()+gp2.getY());
	}
	
}

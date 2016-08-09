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

public class MathUtility {
	public static double EuclideanDistance(Coord c1, Coord c2) {
		return Math.sqrt(Math.pow(c1.getX()-c2.getX(),2)+Math.pow(c1.getY()-c2.getY(),2));
	}
	
	public static double EuclideanDistance(Coordinate c1, Coordinate c2) {
		return Math.sqrt(Math.pow(c1.getX()-c2.getX(),2)+Math.pow(c1.getY()-c2.getY(),2));
	}
	
	public static void rotate(GridPoint point, double degrees){
		rotate(point, degrees, 0, 0);
	}

	public static void rotate(Coordinate point, double degrees) {
		rotate(point, degrees, 0, 0);
	}
	
	public static void rotate(GridPoint point, double degrees, double x_center, double y_center){
		double x = point.getX();
		double y = point.getY();
		degrees = Math.toRadians(degrees);
		
		double x_res = Math.round(((x - x_center) * Math.cos(degrees)) - ((y - y_center) * Math.sin(degrees)) + x_center);
		double y_res = Math.round(((y - y_center) * Math.cos(degrees)) + ((x - x_center) * Math.sin(degrees)) + y_center);
		
		point.setX((int)x_res);
	    point.setY((int)y_res);
	}

	public static void rotate(Coordinate point, double degrees, Coordinate center) {
		rotate(point, degrees, center.getX(), center.getY());
	}

	public static void rotate(Coordinate point, double degrees, double x_center, double y_center) {
		double x = point.getX();
		double y = point.getY();
		degrees = Math.toRadians(degrees);
		
		double x_res = ((x - x_center) * Math.cos(degrees)) - ((y - y_center) * Math.sin(degrees)) + x_center;
		double y_res = ((y - y_center) * Math.cos(degrees)) + ((x - x_center) * Math.sin(degrees)) + y_center;
		
		point.setX(x_res);
	    point.setY(y_res);
	}
	
	public static double convertGridCoordinate(int monodimCoord){
		return (monodimCoord+0.5)*Constants.CA_CELL_SIDE;
	}
	
	public static GridPoint gridPointDifference(GridPoint gp1, GridPoint gp2){
		return new GridPoint(gp1.getX()-gp2.getX(), gp1.getY()-gp2.getY());
	}
	
	public static Coordinate sum(Coordinate c1, Coordinate c2){
		return new Coordinate(c1.getX()+c2.getX(), c1.getY()+c2.getY());
	}
	
	public static GridPoint gridPointSum(GridPoint gp1, GridPoint gp2){
		return new GridPoint(gp1.getX()+gp2.getX(), gp1.getY()+gp2.getY());
	}
	
	public static double average(double x1, double x2){
		return (x1+x2)/2;
	}


}

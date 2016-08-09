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

import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;

public class DiscreteDistances {

    private static final Logger log = Logger.getLogger(DiscreteDistances.class);

	private int radius;
	private Double[][] discreteDistances;
	
	public DiscreteDistances(int radius){
		this.radius = radius;
		initMatrix();
	}
	
	private void initMatrix(){
		int side = radius*2+1;
		discreteDistances = new Double[side][side];
		GridPoint center = new GridPoint(radius,radius);
		for(int i=0;i<discreteDistances.length;i++)
			for(int j=0;j<discreteDistances[i].length;j++)
				discreteDistances[i][j] = Distances.EuclideanDistance(center,new GridPoint(j,i));
	}
	
	public double getDistance(GridPoint center, GridPoint neighbour){
		try{
			return discreteDistances[neighbour.getY()-center.getY()+radius][neighbour.getX()-center.getX()+radius];
		}catch(IndexOutOfBoundsException e){
            log.warn("DiscreteDistances.getDistance() - neighbour out of the range of precalculated distances");
            return Distances.EuclideanDistance(center, neighbour);
		}
	}
}

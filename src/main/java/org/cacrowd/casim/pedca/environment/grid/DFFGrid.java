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

package org.cacrowd.casim.pedca.environment.grid;

import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;

import java.io.File;
import java.io.IOException;

public class DFFGrid extends Grid<Double> {

	final int radius;
	
	
	public DFFGrid(int rows, int cols, int radius) {
		super(rows, cols);
		this.radius = radius;
	}
	
	protected void diffusion(GridPoint gp){
		
		//TODO 
		/*
		Neighbourhood neighbourhood = getNeighbourhood(gp);
		for (int i = 0; i<neighbourhood.size();i++){
			GridPoint neighbour = neighbourhood.get(i);
			//double distance = 
		}
		*/
	}

	protected void decay(GridPoint gp){
		
	}
	
	public Neighbourhood getNeighbourhood(GridPoint gp){
		Neighbourhood neighbourhood = new Neighbourhood();
		int row_gp = gp.getY();
		int col_gp = gp.getX();
		for(int row=row_gp-radius;row<=row_gp+radius;row++)
			for (int col=col_gp-radius;col<=col_gp+radius;col++)
				if (neighbourCondition(row,col))
					neighbourhood.add(new GridPoint(col,row));
		return neighbourhood;
	} 
	
	@Override
	protected void loadFromCSV(File file) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveCSV(String path) throws IOException {
		// TODO Auto-generated method stub

	}


	
}

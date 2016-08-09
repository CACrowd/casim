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

package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.utility.Lottery;

import java.util.ArrayList;

public class Start extends Marker {

	private static final long serialVersionUID = 1L;
	private Double frequency;
	private int totalPedestrians;
	private int generatedPedestrian = 0;
	
	public Start(ArrayList<GridPoint> cells){
		this(0,cells);
	}
	
	public Start(int totalPedestrians, ArrayList<GridPoint> cells){
		super(cells);
		this.totalPedestrians = totalPedestrians;
	}
	
	public Start(double frequency, int totalPedestrians, ArrayList<GridPoint> cells){
		super(cells);
		this.frequency=frequency;
		this.totalPedestrians = totalPedestrians;
	}
	
	public boolean canGenerate(){
		return generatedPedestrian<totalPedestrians;
	}
	
	public int toBeGenerated(){
		if(frequency==null)
			return totalPedestrians - generatedPedestrian;
		int result = frequency.intValue();
		double probability = frequency.doubleValue() - frequency.intValue();
		if (Lottery.simpleExtraction(probability))
			result++;
		return result;
	}
	
	public void notifyGeneration(){
		generatedPedestrian++;
	}
	
	public void setTotalPedestrians(int totalPedestrians){
		this.totalPedestrians = totalPedestrians;
	}
	
	public void setFrequency(double frequency){
		this.frequency = frequency;
	}
}

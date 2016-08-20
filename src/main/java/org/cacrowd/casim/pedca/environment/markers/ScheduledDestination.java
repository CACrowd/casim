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

import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;

import java.util.ArrayList;

public class ScheduledDestination extends DelayedDestination {

	private static final long serialVersionUID = 1L;

	private double[] scheduledTimes;
	private int scheduleIndex;
	private int timeWindow;
	
	public ScheduledDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, double[] scheduleTimes, int timeWindow) {
		super(coordinate, cells, isStairsBorder, 0);
		this.scheduledTimes = scheduleTimes;
		this.scheduleIndex = 0;
		this.timeWindow = timeWindow;
	}

	@Override
	public int waitingTimeForCrossing(double time){
		int dayTime = (int)time; // % 86400;
		int stepToCross = (int)((scheduledTimes[scheduleIndex] - dayTime)/Constants.CA_STEP_DURATION);
		if (stepToCross > 0)
			return stepToCross;
		return 0;
	}
	
	@Override
	public void step(double time) {
		//TODO FIX THIS: just a first implementation of a daily schedule (86400 s) for the simulation of more days..
		int dayTime = (int)time % 86400;
		if (dayTime > scheduledTimes[scheduleIndex]+timeWindow)
			scheduleIndex=(scheduleIndex+1)%scheduledTimes.length;
		else if (dayTime < scheduledTimes[0]+timeWindow)
			scheduleIndex = 0;
	}
	
}

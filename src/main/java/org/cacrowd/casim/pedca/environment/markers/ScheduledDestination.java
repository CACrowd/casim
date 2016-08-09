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
	private int timeShift;
	
	public ScheduledDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, double[] scheduleTimes) {
		super(coordinate, cells, isStairsBorder, 0);
		this.scheduledTimes = scheduleTimes;
		this.scheduleIndex = 0;
		this.timeShift = 240;
	}

	@Override
	public int waitingTimeForCrossing(double time){
		int dayTime = (int)time % 86400;
		int stepToCross = (int)((scheduledTimes[scheduleIndex] - dayTime + timeShift)/Constants.CA_STEP_DURATION);
		if (stepToCross > 0)
			return stepToCross;
		return 0;
	}
	
	@Override
	public void step(double time) {
		int dayTime = (int)time % 86400;
		if (dayTime > scheduledTimes[scheduleIndex]+2*timeShift)
			scheduleIndex=(scheduleIndex+1)%scheduledTimes.length;
	}
	
}

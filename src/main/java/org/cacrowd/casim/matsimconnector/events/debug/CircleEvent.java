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

package org.cacrowd.casim.matsimconnector.events.debug;

import org.matsim.api.core.v01.events.Event;

public class CircleEvent  extends Event{

	private static final String TYPE = "CIRCLE_EVENT";
	private final double x;
	private final double y;
	
	public CircleEvent(double time,double x, double y) {
		super(time);
		this.x = x;
		this.y = y;
	}

	@Override
	public String getEventType() {
		return CircleEvent.TYPE;
	}
	
	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}

}

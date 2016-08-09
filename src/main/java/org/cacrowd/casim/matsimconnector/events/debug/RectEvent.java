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

public class RectEvent extends Event {

	private static final String EVENT_TYPE = "RECT_EVENT";
	private final double tx;
	private final double ty;
	private final double sx;
	private final double sy;
	private final boolean fill;

	public RectEvent(double time, double tx, double ty, double sx, double sy,boolean fill) {
		super(time);
		this.tx = tx;
		this.ty = ty;
		this.sx = sx;
		this.sy = sy;
		this.fill = fill;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
	public double getTx() {
		return this.tx;
	}

	public double getTy() {
		return this.ty;
	}

	public double getSx() {
		return this.sx;
	}

	public double getSy() {
		return this.sy;
	}

	public boolean getFill() {
		return this.fill;
	}

}

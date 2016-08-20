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

import org.cacrowd.casim.matsimconnector.visualizer.cgal.LineSegment;
import org.matsim.api.core.v01.events.Event;


public class LineEvent extends Event {

	private static final String TYPE = "LINE_EVENT";
	
	private final boolean isStatic;
	private final LineSegment s;

	private final int r,g,b,a,minScale;

	private final double dash;

	private final double gap;
	
	public LineEvent(double time, LineSegment s, boolean isStatic) {
		this(time, s, isStatic, 0, 0, 0, 255, 0);
	}
	
	public LineEvent(double time, LineSegment s, boolean isStatic, int r, int g, int b, int a, int minScale) {

		this(time, s, isStatic, r, g, b, a, minScale, 0, 0);
	}
	public LineEvent(double time, LineSegment s, boolean isStatic, int r, int g, int b, int a, int minScale, double dash, double gap) {
		super(time);
		this.s = s;
		this.isStatic = isStatic;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.minScale = minScale;
		this.gap = gap;
		this.dash = dash;
		
	}

	@Override
	public String getEventType() {
		return TYPE;
	}
	
	public LineSegment getSegment() {
		return this.s;
	}
	
	public boolean isStatic() {
		return this.isStatic;
	}

	public int getMinScale() {
		return this.minScale;
	}

	public int getA() {
		return this.a;
	}

	public int getB() {
		return this.b;
	}

	public int getG() {
		return this.g;
	}

	public int getR() {
		return this.r;
	}
	
	public double getGap() {
		return this.gap;
		
	}
	
	public double getDash() {
		return this.dash;
	}

}
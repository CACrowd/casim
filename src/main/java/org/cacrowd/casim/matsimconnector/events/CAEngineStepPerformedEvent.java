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

package org.cacrowd.casim.matsimconnector.events;

import org.matsim.api.core.v01.events.Event;

import java.util.Map;

public class CAEngineStepPerformedEvent extends Event{
	public static final String EVENT_TYPE = "CAEngineStepPerformed";
	public static final String ATTRIBUTE_STEP_COMPUTATION = "stepComputationalTime";
	public static final String ATTRIBUTE_POPULATION_SIZE = "populationSize";
	private final float stepCompTime;
	private final int populationSize;
	
	
	public CAEngineStepPerformedEvent(double time, float stepComputationalTime, int populationSize) {
		super((int)time+1);
		this.stepCompTime = stepComputationalTime;
		this.populationSize = populationSize;
	}
	
	
	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_STEP_COMPUTATION, ""+stepCompTime);
		attr.put(ATTRIBUTE_STEP_COMPUTATION, ""+populationSize);
		return attr;
	}

	public float getStepCompTime() {
		return stepCompTime;
	}
	
	
	public int getPopulationSize() {
		return populationSize;
	}
	
}

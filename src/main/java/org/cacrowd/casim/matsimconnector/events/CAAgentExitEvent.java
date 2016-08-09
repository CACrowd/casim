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

import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.matsim.api.core.v01.events.Event;

import java.util.Map;

public class CAAgentExitEvent extends Event{
	public static final String EVENT_TYPE = "CAAgentExitEvent";
	public static final String ATTRIBUTE_PERSON = "pedestrian";
	private final Pedestrian pedestrian;
	
	public CAAgentExitEvent(double time, Pedestrian pedestrian) {
		super(time);
		this.pedestrian = pedestrian;
	}

	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_PERSON, pedestrian.getId().toString());
		return attr;
	}

	public Pedestrian getPedestrian(){
		return pedestrian;
	}
}

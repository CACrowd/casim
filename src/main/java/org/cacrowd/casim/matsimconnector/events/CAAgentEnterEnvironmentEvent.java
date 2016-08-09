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

public class CAAgentEnterEnvironmentEvent extends Event {
	public static final String EVENT_TYPE = "CAAgentEnterEnvironmentEvent";
	public static final String ATTRIBUTE_PERSON = "pedestrian";
	public static final String ATTRIBUTE_REAL_TIME = "real_time";
	public static final String ATTRIBUTE_CA_LINK = "ca_link";
	private final double realTime;
	private Pedestrian pedestrian;
	private String caLinkId;

	public CAAgentEnterEnvironmentEvent(double time, Pedestrian pedestrian) {
		super((int)time+1);
		this.realTime = time;
		this.pedestrian = pedestrian;
		this.caLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId().toString();
	}
	
	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attr = super.getAttributes();
		attr.put(ATTRIBUTE_PERSON, pedestrian.getId().toString());
		attr.put(ATTRIBUTE_REAL_TIME, Double.toString(this.realTime));
		attr.put(ATTRIBUTE_CA_LINK, caLinkId);
		return attr;
	}
	
	public Pedestrian getPedestrian(){
		return pedestrian;
	}
	
	public String getCALinkId(){
		return caLinkId;
	}
	
	@Override
	public String getEventType() {
		return EVENT_TYPE;
	}
}

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

package org.matsim.core.mobsim.qsim.qnetsimengine;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.network.Link;

public class CALink {
	private final QLinkI ql;
	private NetsimEngineContext context;
	
	public CALink(QLinkI qLinkImpl, NetsimEngineContext context) {
		this.ql = qLinkImpl;
		this.context = context ;
	}

	public Link getLink() {
		return this.ql.getLink();
	}

	public void notifyMoveOverBorderNode(QVehicle vehicle, Id<Link> leftLinkId){
		double now = context.getSimTimer().getTimeOfDay();
		context.getEventsManager().processEvent(new LinkLeaveEvent(
				now, vehicle.getId(), leftLinkId));
		context.getEventsManager().processEvent(new LinkEnterEvent(
				now, vehicle.getId(), getLink().getId()));
	}
}


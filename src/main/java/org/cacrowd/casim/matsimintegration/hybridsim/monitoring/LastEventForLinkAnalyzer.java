/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016-2017 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 *
 */

package org.cacrowd.casim.matsimintegration.hybridsim.monitoring;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;

public class LastEventForLinkAnalyzer implements LinkLeaveEventHandler{

    private Map<Id<Link>, LinkLeaveEvent> lastLeaveEventForLink = new HashMap<Id<Link>, LinkLeaveEvent>();
    
    @Override
    public void handleEvent(LinkLeaveEvent event) {
    	lastLeaveEventForLink.put(event.getLinkId(), event);
    }

    @Override
    public void reset(int iteration) {
        lastLeaveEventForLink.clear();
    }

    public Map<Id<Link>, LinkLeaveEvent> getLastLeaveEventForLink() {
        return new HashMap<Id<Link>, LinkLeaveEvent>(lastLeaveEventForLink);
    }
}

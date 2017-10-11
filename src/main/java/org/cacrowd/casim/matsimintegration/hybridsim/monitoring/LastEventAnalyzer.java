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

import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;

public class LastEventAnalyzer implements LinkLeaveEventHandler {

    private LinkLeaveEvent lastEvent;

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        lastEvent = event;

    }

    @Override
    public void reset(int iteration) {
        lastEvent = null;
    }

    public LinkLeaveEvent getLastEvent() {
        return lastEvent;
    }
}

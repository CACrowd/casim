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

package org.cacrowd.casim.matsimintegration.hybridsim.mscb;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.Event;
import org.matsim.api.core.v01.network.Link;

/**
 * Created by laemmel on 15/12/2016.
 */
public class CongestionEvent extends Event {
    private final double congestionDuration;
    private final double congestionLinkEnterTime;
    private final Id<Link> linkId;

    public CongestionEvent(double time, double congestionLinkEnterTime, double congestionDuration, Id<Link> linkId) {
        super(time);
        this.congestionLinkEnterTime = congestionLinkEnterTime;
        this.congestionDuration = congestionDuration;
        this.linkId = linkId;

    }

    public double getCongestionDuration() {
        return congestionDuration;
    }

    public double getCongestionLinkEnterTime() {
        return congestionLinkEnterTime;
    }

    @Override
    public String getEventType() {

        return null;
    }

    public Id<Link> getLinkId() {
        return linkId;
    }
}

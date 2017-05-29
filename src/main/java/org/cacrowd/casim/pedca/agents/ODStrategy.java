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

package org.cacrowd.casim.pedca.agents;

import org.cacrowd.casim.pedca.environment.markers.Destination;

public class ODStrategy implements Strategy {
    private final Destination destination;

    public ODStrategy(Destination origin, Destination destination) {
        this.destination = destination;
    }

    @Override
    public Destination getDestination() {
        return destination;
    }

    @Override
    public double getDepartureTime() {
        return 0;
    }
}

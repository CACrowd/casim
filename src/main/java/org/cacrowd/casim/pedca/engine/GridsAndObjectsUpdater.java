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

package org.cacrowd.casim.pedca.engine;

import com.google.inject.Inject;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.ActiveGrid;
import org.cacrowd.casim.pedca.environment.markers.DelayedDestination;
import org.cacrowd.casim.pedca.environment.markers.Destination;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class GridsAndObjectsUpdater {
    private ArrayList<ActiveGrid> activeGrids;
    private ArrayList<DelayedDestination> activeDestinations;

    @Inject
    public GridsAndObjectsUpdater(Context context) {
        this.activeGrids = new ArrayList<ActiveGrid>();
        this.activeDestinations = new ArrayList<DelayedDestination>();
        activeGrids.addAll(context.getPedestrianGrids());
        for (Destination dest : context.getMarkerConfiguration().getDestinations())
            if (dest instanceof DelayedDestination)
                activeDestinations.add((DelayedDestination) dest);
    }

    public void step(double time) {
        for (ActiveGrid activeGrid : activeGrids) {
            activeGrid.step();
        }
        for (DelayedDestination dest : activeDestinations)
            dest.step(time);
    }

}

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

package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;

import java.util.ArrayList;

public class DelayedDestination extends TacticalDestination {

    private static final long serialVersionUID = 1L;
    private final int stepToCross;

    public DelayedDestination(int id, Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, int stepToCross) {
        super(id, coordinate, cells, isStairsBorder);
        this.stepToCross = stepToCross;
    }

    /**
     * returns the time (in steps) needed by pedestrians to cross the destination, to design doors or turnstiles.
     *
     * @Input simulation time
     */
    public int waitingTimeForCrossing(double time) {
        return stepToCross;
    }

    public void step(double time) {
        //DO NOTHING
    }
}

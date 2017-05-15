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

package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.pedca.utility.Constants;

import java.util.ArrayList;

public class ConstrainedFlowDestination extends DelayedDestination {
    private static final long serialVersionUID = 1L;

    //to model turnstiles or other doors implying delays to walk through - if 0 means that the object is just a set of normal walkable cells
    private final float stepToCross;
    private float currentStepToCross;

    public ConstrainedFlowDestination(Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder, float flowCapacity) {
        super(coordinate, cells, isStairsBorder, 0);
        if (flowCapacity == 0)
            this.stepToCross = 0;
        else
            this.stepToCross = (float) ((1 / flowCapacity) / Constants.STEP_DURATION);
        currentStepToCross = 0;
    }

    @Override
    public int waitingTimeForCrossing(double time) {
        int result = (int) currentStepToCross;
        currentStepToCross += stepToCross;
        return result;
    }

    @Override
    public void step(double time) {
        if (currentStepToCross > 0)
            --currentStepToCross;
    }

}

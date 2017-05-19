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

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.utility.Lottery;

public class Shadow extends PhysicalObject {
    private final int step;
    private final int duration;
    private final int pedestrianId;

    public Shadow(int step, GridPoint position, int pedestrianId, double duration) {
        this.step = step;
        this.position = position;
        this.pedestrianId = pedestrianId;
        if (Lottery.simpleExtraction(duration - (int) duration))
            this.duration = (int) duration + 1;
        else
            this.duration = (int) duration;
    }

    public int getExpirationTime() {
        return step + duration;
    }

    public int getStep() {
        return step;
    }

    public int getDuration() {
        return duration;
    }

    public int getPedestrianId() {
        return pedestrianId;
    }

    @Override
    public String toString() {
        return "shadow";
    }
}

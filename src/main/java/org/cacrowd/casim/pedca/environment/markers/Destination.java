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

import java.util.ArrayList;

public class Destination extends Marker {

    private static final long serialVersionUID = 1L;
    private int level;

    public Destination(ArrayList<GridPoint> cells) {
        super(cells);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}

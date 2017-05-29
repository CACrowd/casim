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

import java.io.Serializable;
import java.util.List;

public abstract class Marker implements Serializable {

    private static final long serialVersionUID = 1L;
    protected List<GridPoint> cells;

    public Marker(List<GridPoint> cells) {
        this.cells = cells;
    }

    public List<GridPoint> getCells() {
        return cells;
    }

    public int size() {
        return cells.size();
    }

    public GridPoint get(int i) {
        return cells.get(i);
    }

}

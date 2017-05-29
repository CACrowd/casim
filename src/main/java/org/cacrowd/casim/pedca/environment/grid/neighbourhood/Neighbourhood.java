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

package org.cacrowd.casim.pedca.environment.grid.neighbourhood;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;

import java.util.ArrayList;

public class Neighbourhood {
    private ArrayList<GridPoint> neighbourhood;

    public Neighbourhood() {
        neighbourhood = new ArrayList<GridPoint>();
    }

    public Neighbourhood(ArrayList<GridPoint> neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public void add(GridPoint gp) {
        neighbourhood.add(gp);
    }

    public GridPoint get(int i) {
        return neighbourhood.get(i);
    }

    public int size() {
        return neighbourhood.size();
    }

    public ArrayList<GridPoint> getObjects() {
        return neighbourhood;
    }
}

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
import org.cacrowd.casim.pedca.utility.Constants;

import java.util.ArrayList;

public class TacticalDestination extends Destination {

    private static final long serialVersionUID = 1L;
    private final Coordinate coordinate;
    private boolean isStairsBorder;
    private double width;


    public TacticalDestination(int id, Coordinate coordinate, ArrayList<GridPoint> cells, boolean isStairsBorder) {
        super(id, cells);
        this.coordinate = coordinate;
        this.isStairsBorder = isStairsBorder;
        calculateWidth();
    }

    public boolean isStairsBorder() {
        return isStairsBorder;
    }

    /**
     * TODO: till now the width is calculated by only considering
     * cases where the destination represent a perfectly horizontal
     * or vertical set of cells
     */
    private void calculateWidth() {
        width = cells.size() * Constants.CELL_SIZE;
    }

    public double getWidth() {
        return width;
    }

    public int getID() {
        return getLevel();
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }


}

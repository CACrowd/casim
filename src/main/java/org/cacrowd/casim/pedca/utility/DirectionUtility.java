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

package org.cacrowd.casim.pedca.utility;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;

import java.util.HashMap;
import java.util.Map;

public class DirectionUtility {
    private static DirectionConverter directionConverter = new DirectionConverter();

    public static Heading convertGridPointToHeading(GridPoint gp) {
        if (gp.getX() > 1)
            gp.setX(1);
        else if (gp.getX() < -1)
            gp.setX(-1);
        if (gp.getY() > 1)
            gp.setY(1);
        else if (gp.getY() < -1)
            gp.setY(-1);
        return directionConverter.gridPointToHeading.get(gp);
    }

    public static GridPoint convertHeadingToGridPoint(Heading h) {
        return directionConverter.headingToGridPoint.get(h);
    }

    public enum Heading {
        N, NE, NW, S, SE, SW, E, W, X
    }
}

class DirectionConverter {
    public final Map<GridPoint, DirectionUtility.Heading> gridPointToHeading = new HashMap<GridPoint, DirectionUtility.Heading>();
    public final Map<DirectionUtility.Heading, GridPoint> headingToGridPoint = new HashMap<DirectionUtility.Heading, GridPoint>();

    public DirectionConverter() {
        gridPointToHeading.put(new GridPoint(0, 0), DirectionUtility.Heading.X);
        headingToGridPoint.put(DirectionUtility.Heading.X, new GridPoint(0, 0));

        gridPointToHeading.put(new GridPoint(1, 0), DirectionUtility.Heading.E);
        headingToGridPoint.put(DirectionUtility.Heading.E, new GridPoint(1, 0));

        gridPointToHeading.put(new GridPoint(1, -1), DirectionUtility.Heading.SE);
        headingToGridPoint.put(DirectionUtility.Heading.SE, new GridPoint(1, -1));

        gridPointToHeading.put(new GridPoint(0, -1), DirectionUtility.Heading.S);
        headingToGridPoint.put(DirectionUtility.Heading.S, new GridPoint(0, -1));

        gridPointToHeading.put(new GridPoint(-1, -1), DirectionUtility.Heading.SW);
        headingToGridPoint.put(DirectionUtility.Heading.SW, new GridPoint(-1, -1));

        gridPointToHeading.put(new GridPoint(-1, 0), DirectionUtility.Heading.W);
        headingToGridPoint.put(DirectionUtility.Heading.W, new GridPoint(-1, 0));

        gridPointToHeading.put(new GridPoint(-1, 1), DirectionUtility.Heading.NW);
        headingToGridPoint.put(DirectionUtility.Heading.NW, new GridPoint(-1, 1));

        gridPointToHeading.put(new GridPoint(0, 1), DirectionUtility.Heading.N);
        headingToGridPoint.put(DirectionUtility.Heading.N, new GridPoint(0, 1));

        gridPointToHeading.put(new GridPoint(1, 1), DirectionUtility.Heading.NE);
        headingToGridPoint.put(DirectionUtility.Heading.NE, new GridPoint(1, 1));
    }

}

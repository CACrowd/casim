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
import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;

public class NeighbourhoodUtility {

    public static Neighbourhood calculateMooreNeighbourhood(GridPoint neighbour) {
        Neighbourhood result = new Neighbourhood();
        for (int y = neighbour.getY() - 1; y <= neighbour.getY() + 1; y++)
            for (int x = neighbour.getX() - 1; x <= neighbour.getX() + 1; x++)
                result.add(new GridPoint(x, y));
        return result;
    }

    public static Neighbourhood calculateVonNeumannNeighbourhood(GridPoint neighbour) {
        Neighbourhood result = new Neighbourhood();
        for (int y = neighbour.getY() - 1; y <= neighbour.getY() + 1; y++)
            for (int x = neighbour.getX() - 1; x <= neighbour.getX() + 1; x++)
                if (x == neighbour.getX() || y == neighbour.getY())
                    result.add(new GridPoint(x, y));
        return result;
    }

}

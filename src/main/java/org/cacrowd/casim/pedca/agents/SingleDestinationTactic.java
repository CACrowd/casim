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

package org.cacrowd.casim.pedca.agents;

import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.FloorFieldsGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;

public class SingleDestinationTactic implements Tactic {

    private final Destination destination;
    private final Context context;

    public SingleDestinationTactic(Destination destination, Context context) {
        this.destination = destination;
        this.context = context;

    }


    @Override
    public double getStaticFFValue(GridPoint gridPoint) {
        int level = destination.getLevel();
        FloorFieldsGrid ff = context.getFloorFieldsGrid();
        return ff.getCellValue(level, gridPoint);
    }

    @Override
    public boolean exit(GridPoint position) {
        int level = destination.getLevel();
        FloorFieldsGrid ff = context.getFloorFieldsGrid();
        return ff.getCellValue(level, position) == 0.;
    }
}

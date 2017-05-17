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

import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.FloorFieldsGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.utility.Constants;

import java.util.List;

public class SimpleTargetChainTactic implements Tactic {

    private static final Logger log = Logger.getLogger(SimpleTargetChainTactic.class);

    private final List<Destination> targets;
    private final Context context;
    private final Strategy strategy;
    private int currentIdx = 0;

    private Destination currentTacticalDestination;

    public SimpleTargetChainTactic(Strategy strategy, List<Destination> targets, Context context) {
        this.targets = targets;
        this.context = context;
        this.strategy = strategy;
        this.currentTacticalDestination = targets.get(0);
    }

    @Override
    public double getStaticFFValue(GridPoint gridPoint) {

        int level = currentTacticalDestination.getLevel();

        FloorFieldsGrid ff = context.getFloorFieldsGrid();

        return ff.getCellValue(level, gridPoint);
    }

    @Override
    public boolean exit(GridPoint gridPoint) {

        FloorFieldsGrid ff = context.getFloorFieldsGrid();
        double pot = ff.getCellValue(currentTacticalDestination.getLevel(), gridPoint);

        if (pot == 0 && currentIdx < targets.size() - 1) {
            currentIdx++;
            this.currentTacticalDestination = targets.get(currentIdx);
            pot = ff.getCellValue(this.currentTacticalDestination.getLevel(), gridPoint);
        } else if (pot == Constants.MAX_FF_VALUE) {
            //got pushed back?
            log.info("Agent probably got pushed back");
            currentIdx--;
            this.currentTacticalDestination = targets.get(currentIdx);
            pot = ff.getCellValue(this.currentTacticalDestination.getLevel(), gridPoint);
        } else if (pot == 0 && currentIdx == targets.size() - 1) {
            this.currentTacticalDestination = this.strategy.getDestination();
            pot = ff.getCellValue(this.currentTacticalDestination.getLevel(), gridPoint);
        }
        return pot == 0.;
    }
}

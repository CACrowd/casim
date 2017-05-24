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
 */

package org.cacrowd.casim.pedca.engine;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.pedca.utility.NeighbourhoodUtility;

import java.util.*;


public class SimpleAreaTransitionHandler implements TransitionHandler {

    private static final Logger log = Logger.getLogger(SimpleAreaTransitionHandler.class);

    private final Map<GridPoint, TransitionArea> gridPointAreaMap = new HashMap<>();

    private final List<TransitionArea> transitionAreas = new ArrayList<>();
    private final Context context;
    private Set<Agent> scheduledArrivals = new LinkedHashSet<>();

    @Inject
    public SimpleAreaTransitionHandler(Context context) {
        this.context = context;
        init();
    }

    private void init() {


        final EnvironmentGrid env = this.context.getEnvironmentGrid();
        Queue<GridPoint> gps = new LinkedList<>();
        for (int row = 0; row < env.getRows(); row++) {
            for (int col = 0; col < env.getColumns(); col++) {
                gps.add(new GridPoint(col, row));
            }
        }
        while (gps.peek() != null) {
            GridPoint seed = gps.poll();
            if (gridPointAreaMap.containsKey(seed) || env.getCellValue(seed) != Constants.ENV_WALKABLE_CELL) {
                continue;
            }
            spreadSeed(seed);
        }


    }

    private void spreadSeed(GridPoint seed) {
        final EnvironmentGrid env = this.context.getEnvironmentGrid();
        SimpleTransitionArea a = new SimpleTransitionArea(this.context);
        this.transitionAreas.add(a);
        Queue<GridPoint> open = new LinkedList<>();
        open.add(seed);
        while (open.peek() != null) {
            GridPoint cand = open.poll();
            if (gridPointAreaMap.containsKey(cand) || env.getCellValue(cand) != Constants.ENV_WALKABLE_CELL) {
                continue;
            }
            a.pointList.add(cand);
            gridPointAreaMap.put(cand, a);
            Neighbourhood nb = NeighbourhoodUtility.calculateVonNeumannNeighbourhood(cand);
            open.addAll(nb.getObjects());
        }
    }


    @Override
    public void step(double time) {

        Iterator<Agent> it = context.getPopulation().getPedestrians().iterator();
        while (it.hasNext()) {
            Agent cand = it.next();
            if (cand.isAboutToLeave()) {
                context.getPedestrianGrid().removePedestrian(cand.getPosition(), cand);
                it.remove();
            }
        }
        this.transitionAreas.forEach(a -> a.step(time));//try out parallel stream
    }

    @Override
    public void scheduleForDeparture(Agent a) {

        GridPoint desiredStartPosition = a.getPosition();
        TransitionArea ta = this.gridPointAreaMap.get(desiredStartPosition);
        ta.scheduleDeparture(a);
        
    }

    @Override
    public void scheduleForArrival(Agent a) {
//        log.info(a.getID());
        this.scheduledArrivals.add(a);
    }

    public Map<GridPoint, TransitionArea> getTransitionAreas() {
        return gridPointAreaMap;
    }
    
}

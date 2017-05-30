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

package org.cacrowd.casim.hybridsim.engine;

import com.google.inject.Inject;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.SimpleTransitionArea;
import org.cacrowd.casim.pedca.engine.TransitionArea;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.pedca.utility.NeighbourhoodUtility;
import org.cacrowd.casim.proto.HybridSimProto;

import java.util.*;


public class SimpleHybridTransitionHandler implements HybridTransitionHandler {

    private final Map<GridPoint, TransitionArea> gridPointAreaMap = new HashMap<>();
    private final List<TransitionArea> transitionAreas = new ArrayList<>();
    @Inject
    private Context context;
    private Queue<Agent> scheduledDepartures = new LinkedList<>();

    @Override
    public void step(double time) {
        while (scheduledDepartures.peek() != null) {
            Agent a = scheduledDepartures.poll();
            GridPoint desiredStartPosition = a.getPosition();
            TransitionArea ta = this.gridPointAreaMap.get(desiredStartPosition);
            ta.scheduleDeparture(a);
        }
        this.transitionAreas.forEach(a -> a.step(time));
    }

    @Override
    public void scheduleForDeparture(Agent a) {
        this.scheduledDepartures.add(a);
    }

    @Override
    public void init() {
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
    public HybridSimProto.Agents queryRetrievableAgents() {
        HybridSimProto.Agents.Builder abs = HybridSimProto.Agents.newBuilder();
        HybridSimProto.Agent.Builder ab = HybridSimProto.Agent.newBuilder();
        context.getPopulation().getPedestrians().stream().filter(Agent::isAboutToLeave).map(a -> ab.setId(a.getID()).build()).forEach(abs::addAgents);
        return abs.build();
    }

    @Override
    public void confirmRetrievedAgents(HybridSimProto.Agents confirmed) {
        confirmed.getAgentsList().forEach(a -> {
            context.getPopulation().remove(a.getId()).delete();

        });
    }
}

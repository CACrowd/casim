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
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.*;
import org.cacrowd.casim.proto.HybridSimProto;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.utility.rasterizer.Edge;
import org.cacrowd.casim.utility.rasterizer.Rasterizer;

import java.util.List;
import java.util.stream.Collectors;

public class HybridSimulationEngine {

    @Inject
    private AgentsGenerator agentGenerator;
    @Inject
    private AgentsUpdater agentUpdater;
    @Inject
    private ConflictSolver conflictSolver;
    @Inject
    private AgentMover agentMover;
    @Inject
    private Context context;
    @Inject
    private GridsAndObjectsUpdater activeObjectsUpdater;
    @Inject
    private SimulationObserver observer;
    @Inject
    private TransitionHandler transitionHandler;
    @Inject
    private Rasterizer rasterizer;

    public void loadEnvironment(HybridSimProto.Scenario request) {
        List<Edge> res = request.getEdgesList().stream().map(he -> {


            Rasterizer.EdgeType type;
            switch (he.getType()) {
                case OBSTACLE:
                    type = Rasterizer.EdgeType.WALL;
                    break;
                case TRANSITION:
                    type = Rasterizer.EdgeType.TRANSITION_INTERNAL;
                    break;
                default:
                    type = Rasterizer.EdgeType.WALL;
            }
            return new Edge(he.getId(), he.getC0().getX(), he.getC0().getY(),
                    he.getC1().getX(), he.getC1().getY(), type);

        }).collect(Collectors.toList());

        rasterizer.buildContext(res);
        observer.observerEnvironmentGrid();
    }

//    private final Rasterizer rasterizer = new Rasterizer();


}

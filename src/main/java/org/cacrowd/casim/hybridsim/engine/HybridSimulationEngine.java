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
import com.vividsolutions.jts.geom.Envelope;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.*;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfigurationImpl;
import org.cacrowd.casim.pedca.utility.Constants;
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

        Envelope e = new Envelope();
        res.forEach(edge -> {
            e.expandToInclude(edge.getX0(), edge.getY0());
            e.expandToInclude(edge.getX1(), edge.getY1());
        });

        int rows = (int) (e.getHeight() / Constants.CELL_SIZE) + 1;
        int cols = (int) (e.getWidth() / Constants.CELL_SIZE) + 1;
        EnvironmentGrid grid = new EnvironmentGrid(rows, cols, 0, 0);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid.setCellValue(row, col, -1);
            }
        }
        Rasterizer rasterizer = new Rasterizer(grid);
        rasterizer.rasterize(res);

        MarkerConfiguration markerConfiguration = new MarkerConfigurationImpl();
        context.initialize(grid, markerConfiguration);
        observer.observerEnvironmentGrid();
    }

//    private final Rasterizer rasterizer = new Rasterizer();


}

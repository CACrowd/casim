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
import org.cacrowd.casim.pedca.agents.*;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.*;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
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
    private HybridTransitionHandler transitionHandler;
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
        transitionHandler.init();
        activeObjectsUpdater.init();
        observer.observerEnvironmentGrid();
    }

    public boolean tryAddAgent(HybridSimProto.Agent request) {


        GridPoint enterLocation = context.getEnvironmentGrid().coordinate2GridPoint(new Coordinate(request.getEnterLocation().getX(), request.getEnterLocation().getY()));


        Destination orig = context.getMarkerConfiguration().getDestination(request.getDestsList().get(0).getId());
        Destination dest = context.getMarkerConfiguration().getDestination(request.getDestsList().get(request.getDestsList().size() - 1).getId());


        Strategy strategy = new ODStrategy(orig, dest);
        List<Destination> intermediate = request.getDestsList().stream().limit(request.getDestsList().size() - 1).skip(1).map(d -> context.getMarkerConfiguration().getDestination(d.getId())).collect(Collectors.toList());


        Tactic tactic = new SimpleTargetChainTactic(strategy, intermediate, context);
        Agent a1 = new Agent(request.getId(), enterLocation, tactic, context);
        transitionHandler.scheduleForDeparture(a1);

        return true;
    }

    public void doSimInterval(HybridSimProto.LeftClosedRightOpenTimeInterval request) {

        if (context.getTimeOfDay() <= 0 || context.getTimeOfDay() > request.getFromTimeIncluding()) {
            context.setTimeOfDay(request.getFromTimeIncluding());
        }

        for (double time = context.getTimeOfDay(); time < request.getToTimeExcluding(); time += Constants.STEP_DURATION) {
            context.setTimeOfDay(time);
            doSimStep(time);
            observer.observerDensityGrid();
            observer.observePopulation();

//            //for movie creation to reach a higher (pseudo) frame rate
//            for (double visTime = time; visTime < time + Constants.STEP_DURATION; visTime += Constants.STEP_DURATION / 3) {
//                context.setTimeOfDay(visTime);
//                observer.observerDensityGrid();
//                observer.observePopulation();
//            }
        }

    }

    private void doSimStep(double time) {
        transitionHandler.step(time);
        agentUpdater.step();
        conflictSolver.step();
        agentMover.step(time);
        activeObjectsUpdater.step(time);

    }

    public HybridSimProto.Trajectories receiveTrajectories() {
        HybridSimProto.Trajectories.Builder tbs = HybridSimProto.Trajectories.newBuilder();
        HybridSimProto.Trajectory.Builder tb = HybridSimProto.Trajectory.newBuilder();
        HybridSimProto.Destination.Builder db = HybridSimProto.Destination.newBuilder();
        context.getPopulation().getPedestrians().forEach(p -> {
            Coordinate c = context.getEnvironmentGrid().gridPoint2Coordinate(p.getPosition());
            tbs.addTrajectories(tb.setId(p.getID()).setX(c.getX()).setY(c.getY()).setCurrentDest(db.setId(p.getCurrentDestination().getId())).build());
        });
        return tbs.build();
    }

    public HybridSimProto.Agents getRetrievableAgents() {
        return transitionHandler.queryRetrievableAgents();
    }

    public void confirmRetrievedAgents(HybridSimProto.Agents request) {
        transitionHandler.confirmRetrievedAgents(request);
    }


//    private final Rasterizer rasterizer = new Rasterizer();


}

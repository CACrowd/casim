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

package org.cacrowd.casim.pedca.engine;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.scenarios.ContextGenerator;
import org.cacrowd.casim.scenarios.EnvironmentGenerator;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.visualizer.VisualizerEngine;

public class SimulationEngine {

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

    public static void main(String[] args) {
        Context context = ContextGenerator.getBidCorridorContext(8, 150);

        EnvironmentGrid environmentGrid = context.getEnvironmentGrid();
        Destination east = EnvironmentGenerator.getCorridorEastDestination(environmentGrid);
        east.setLevel(1);
        int id = 0;
        for (int col = 0; col < 50; col += 4) {
            for (int row = 1; row < 7; row += 4) {
                Agent a1 = new Agent(id++, new GridPoint(col, row), east, context);
                context.getPopulation().addPedestrian(a1);
                context.getPedestrianGrid().addPedestrian(new GridPoint(col, row), a1);
            }
        }
        Destination west = EnvironmentGenerator.getCorridorWestDestination(environmentGrid);
        west.setLevel(0);
        id = -1;
        for (int col = 149; col > 100; col -= 4) {
            for (int row = 1; row < 7; row += 4) {
                Agent b1 = new Agent(id--, new GridPoint(col, row), west, context);
                context.getPopulation().addPedestrian(b1);
                context.getPedestrianGrid().addPedestrian(new GridPoint(col, row), b1);
            }
        }

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).toInstance(context);
                bind(AgentMover.class).to(CAAgentMover.class);
                bind(SimulationObserver.class).to(VisualizerEngine.class);
            }
        });


        SimulationEngine e = injector.getInstance(SimulationEngine.class);
        e.run();
    }

    public void run() {

        observer.observerEnvironmentGrid();
        for (double time = 0; time < 100; time += Constants.STEP_DURATION) {
            context.setTimeOfDay(time);


            doSimStep(time);
            observer.observerDensityGrid();
            observer.observePopulation();

            //           //for movie creation to reach a higher (pseudo) frame rate
//            for (double visTime = time; visTime < time + Constants.STEP_DURATION; visTime += Constants.STEP_DURATION / 6) {
//                context.setTimeOfDay(visTime);
//                observer.observerDensityGrid();
//                observer.observePopulation();
//            }
        }
    }

    public void doSimStep(double time) {
        //Log.log("STEP at: "+time);
        agentUpdater.step();
        conflictSolver.step();
        agentMover.step(time);
        activeObjectsUpdater.step(time);
    }

}

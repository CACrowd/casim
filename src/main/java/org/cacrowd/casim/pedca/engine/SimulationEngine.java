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
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.scenarios.ContextGenerator;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.visualizer.VisualizerEngine;

import java.util.ArrayList;

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
        Context context = ContextGenerator.getCorridorContext(8, 50, 100);
        ArrayList<GridPoint> east = new ArrayList<>();
        east.add(new GridPoint(50, 1));
        east.add(new GridPoint(50, 2));
        east.add(new GridPoint(50, 3));
        east.add(new GridPoint(50, 4));
        east.add(new GridPoint(50, 5));
        east.add(new GridPoint(50, 6));
        east.add(new GridPoint(50, 7));

        Agent a = new Agent(0, new GridPoint(0, 2), new Destination(east), context);

        context.getPopulation().addPedestrian(a);
        context.getPedestrianGrid().addPedestrian(new GridPoint(0, 2), a);


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
            observer.observePopulation();
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

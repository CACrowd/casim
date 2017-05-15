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
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.scenarios.ContextGenerator;

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

    public static void main(String[] args) {
        Context context = ContextGenerator.getCorridorContext(4, 16, 100);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).toInstance(context);
                bind(AgentMover.class).to(CAAgentMover.class);
            }
        });


        SimulationEngine e = injector.getInstance(SimulationEngine.class);
        e.doSimStep(1);
    }

    //FOR MATSIM CONNECTOR
    public void doSimStep(double time) {
        //Log.log("STEP at: "+time);
        agentUpdater.step();
        conflictSolver.step();
        agentMover.step(time);
        activeObjectsUpdater.step(time);
    }

}

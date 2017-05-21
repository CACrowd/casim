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

package org.cacrowd.casim.pedca.agents;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.*;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.utility.CASimRandom;
import org.cacrowd.casim.scenarios.ContextGenerator;
import org.cacrowd.casim.scenarios.EnvironmentGenerator;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class AgentTests {


    @Before
    public void reset() {
        CASimRandom.reset(42);
    }


    @Test
    public void testOneStep() {

        Context context = ContextGenerator.getBidCorridorContext(3, 2);
        EnvironmentGrid environmentGrid = context.getEnvironmentGrid();
        Destination east = EnvironmentGenerator.getCorridorEastDestination(environmentGrid);
        east.setLevel(1);
        Tactic tactic = new SingleDestinationTactic(east, context);
        Agent a1 = new Agent(0, new GridPoint(0, 1), tactic, context, null);
        context.getPopulation().addPedestrian(a1);
        context.getPedestrianGrid().addPedestrian(new GridPoint(0, 1), a1);
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).toInstance(context);
            }
        });

        AgentsUpdater agentUpdater = injector.getInstance(AgentsUpdater.class);
        agentUpdater.step();

        GridPoint expected = new GridPoint(1, 1);

        GridPoint actual = a1.getNewPosition();

        assertThat(actual, is(expected));

    }

    @Test
    public void testTimeGap() {

        Context context = ContextGenerator.getBidCorridorContext(3, 3);
        TransitionHandler ta = new SimpleTransitionHandler(context);
        EnvironmentGrid environmentGrid = context.getEnvironmentGrid();
        Destination east = EnvironmentGenerator.getCorridorEastDestination(environmentGrid);
        east.setLevel(1);
        Tactic tactic = new SingleDestinationTactic(east, context);
        Agent a1 = new Agent(0, new GridPoint(0, 1), tactic, context, ta);
        context.getPopulation().addPedestrian(a1);
        context.getPedestrianGrid().addPedestrian(new GridPoint(0, 1), a1);
        Agent a2 = new Agent(1, new GridPoint(1, 1), tactic, context, ta);
        context.getPopulation().addPedestrian(a2);
        context.getPedestrianGrid().addPedestrian(new GridPoint(1, 1), a2);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).toInstance(context);
                bind(AgentMover.class).to(CAAgentMover.class);
//                bind(TransitionHandler.class).toInstance(ta);
            }
        });

        AgentsUpdater agentUpdater = injector.getInstance(AgentsUpdater.class);
        AgentMover agentMover = injector.getInstance(AgentMover.class);
        GridsAndObjectsUpdater activeObjectsUpdater = injector.getInstance(GridsAndObjectsUpdater.class);

        agentUpdater.step();
        {
            GridPoint expected = new GridPoint(0, 1);
            GridPoint actual = a1.getNewPosition();
            assertThat(actual, is(expected));
        }
        agentMover.step(0.);
        activeObjectsUpdater.step(0.);


        agentUpdater.step();
        {
            GridPoint expected = new GridPoint(0, 1);
            GridPoint actual = a1.getNewPosition();
            assertThat(actual, is(expected));
        }
        agentMover.step(0.3);
        activeObjectsUpdater.step(0.3);

        agentUpdater.step();
        {
            GridPoint expected = new GridPoint(0, 1);
            GridPoint actual = a1.getNewPosition();
            assertThat(actual, is(expected));
        }
        agentMover.step(0.6);
        activeObjectsUpdater.step(0.6);

        agentUpdater.step();
        {
            GridPoint expected = new GridPoint(1, 1);
            GridPoint actual = a1.getNewPosition();
            assertThat(actual, is(expected));
        }
        agentMover.step(0.9);
        activeObjectsUpdater.step(0.9);

    }

    @Test
    public void testSwap() {
        Context context = ContextGenerator.getBidCorridorContext(3, 2);
        EnvironmentGrid environmentGrid = context.getEnvironmentGrid();
        Destination east = EnvironmentGenerator.getCorridorEastDestination(environmentGrid);
        east.setLevel(1);
        Tactic tacticEast = new SingleDestinationTactic(east, context);

        Agent a1 = new Agent(0, new GridPoint(0, 1), tacticEast, context, null);
        context.getPopulation().addPedestrian(a1);
        context.getPedestrianGrid().addPedestrian(new GridPoint(0, 1), a1);

        Destination west = EnvironmentGenerator.getCorridorWestDestination(environmentGrid);
        Tactic tacticWest = new SingleDestinationTactic(west, context);

        Agent b1 = new Agent(1, new GridPoint(1, 1), tacticWest, context, null);
        context.getPopulation().addPedestrian(b1);
        context.getPedestrianGrid().addPedestrian(new GridPoint(1, 1), b1);
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).toInstance(context);
                bind(AgentMover.class).to(CAAgentMover.class);
            }
        });

        AgentsUpdater agentUpdater = injector.getInstance(AgentsUpdater.class);
        AgentMover agentMover = injector.getInstance(AgentMover.class);
        GridsAndObjectsUpdater activeObjectsUpdater = injector.getInstance(GridsAndObjectsUpdater.class);
        ConflictSolver conflictSolver = injector.getInstance(ConflictSolver.class);

        for (int i = 0; i <= 10; i++) {
            double time = i * 0.3;
            agentUpdater.step();
            conflictSolver.step();
            agentMover.step(time);
            activeObjectsUpdater.step(time);

            GridPoint expected1 = new GridPoint(0, 1);
            GridPoint expected2 = new GridPoint(1, 1);
            if (i == 10) {    //swap
                GridPoint tmp = expected2;
                expected2 = expected1;
                expected1 = tmp;
            }

            GridPoint actual1 = a1.getPosition();
            assertThat(actual1, is(expected1));
            GridPoint actual2 = b1.getPosition();
            assertThat(actual2, is(expected2));
        }
    }

}

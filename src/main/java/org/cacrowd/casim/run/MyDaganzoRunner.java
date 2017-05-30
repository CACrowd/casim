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

package org.cacrowd.casim.run;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.agents.*;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.*;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.scenarios.EnvironmentGenerator;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.visualizer.VisualizerEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregor Laemmel on 16.05.2017.
 */
public class MyDaganzoRunner {

    private static final Logger log = Logger.getLogger(MyDaganzoRunner.class);

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        log.info("start");
        Context context = createContextWithResourceEnvironmentFileV2("src/main/resources/environmentGrid_Dag12.csv");


        List<Destination> dests = context.getMarkerConfiguration().getTacticalDestinations();

        List<Destination> detour = new ArrayList<>();
        detour.add(dests.get(7));
        detour.add(dests.get(4));
        detour.add(dests.get(2));
        detour.add(dests.get(0));
        detour.add(dests.get(1));
        detour.add(dests.get(3));
        detour.add(dests.get(5));
        detour.add(dests.get(10));

        List<Destination> bottleneck = new ArrayList<>();
        bottleneck.add(dests.get(7));
        bottleneck.add(dests.get(8));
        bottleneck.add(dests.get(9));
        bottleneck.add(dests.get(10));


        Strategy strategy = new ODStrategy(dests.get(6), dests.get(11));


//        TransitionHandler transistionHandler = new SimpleAreaTransitionHandler(context);


        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).toInstance(context);
                bind(AgentMover.class).to(CAAgentMover.class);
                bind(SimulationObserver.class).to(VisualizerEngine.class);
                bind(TransitionHandler.class).to(SimpleAreaTransitionHandler.class).in(Singleton.class);
            }
        });

        TransitionHandler handler = injector.getInstance(TransitionHandler.class);

        int id = 0;

        for (int coeff = 0; coeff < 10000; coeff++) {

            for (int row = 42; row >= 39; row--) {

                Tactic tactic = new SimpleTargetChainTactic(strategy, bottleneck, context);
                Agent a1 = new Agent(id++, new GridPoint(2, row), tactic, context);
                handler.scheduleForDeparture(a1);
//            context.getPopulation().addPedestrian(a1);
//            context.getPedestrianGrid().addPedestrian(new GridPoint(2, row), a1);
            }


            for (int row = 42; row >= 39; row--) {
                Tactic tactic = new SimpleTargetChainTactic(strategy, detour, context);
                Agent a1 = new Agent(-(id++), new GridPoint(3, row), tactic, context);
                handler.scheduleForDeparture(a1);
//            context.getPopulation().addPedestrian(a1);
//            context.getPedestrianGrid().addPedestrian(new GridPoint(3, row), a1);
            }
        }


        SimulationEngine e = injector.getInstance(SimulationEngine.class);
        e.run();

        log.info("finished");

    }

    public static Context createContextWithResourceEnvironmentFileV2(String envFileName) {
        EnvironmentGrid environmentGrid = null;
        MarkerConfiguration markerConfiguration = null;
        try {
            File environmentFile = new File(envFileName);
            environmentGrid = new EnvironmentGrid(environmentFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        markerConfiguration = EnvironmentGenerator.searchFinalDestinations(environmentGrid);
        EnvironmentGenerator.addTacticalDestinations(markerConfiguration, environmentGrid);
        Context context = new Context(environmentGrid, markerConfiguration);
        return context;
    }


}

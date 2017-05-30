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

package org.cacrowd.casim.hybridsim.run;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cacrowd.casim.hybridsim.engine.HybridTransitionHandler;
import org.cacrowd.casim.hybridsim.engine.SimpleHybridTransitionHandler;
import org.cacrowd.casim.hybridsim.grpc.GRPCServer;
import org.cacrowd.casim.pedca.engine.AgentMover;
import org.cacrowd.casim.pedca.engine.CAAgentMover;
import org.cacrowd.casim.utility.NullObserver;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.visualizer.VisualizerEngine;

import java.awt.*;

public class HybridsimServer {

    public static void main(String[] args) throws Exception {


        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
//                bind(Context.class).to(Context.class);
                bind(AgentMover.class).to(CAAgentMover.class);
                if (GraphicsEnvironment.isHeadless()) {
                    //headless mode (e.g. in docker environment)
                    bind(SimulationObserver.class).to(NullObserver.class);
                } else {
                    bind(SimulationObserver.class).to(VisualizerEngine.class);
                }

                bind(HybridTransitionHandler.class).to(SimpleHybridTransitionHandler.class);
            }
        });


        GRPCServer server = injector.getInstance(GRPCServer.class);
        server.run();
    }


}

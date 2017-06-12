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

package org.cacrowd.casim.integration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cacrowd.casim.hybridsim.engine.HybridTransitionHandler;
import org.cacrowd.casim.hybridsim.engine.SimpleHybridTransitionHandler;
import org.cacrowd.casim.hybridsim.grpc.GRPCServer;
import org.cacrowd.casim.hybridsim.testclient.HybridsimTestClient;
import org.cacrowd.casim.pedca.engine.AgentMover;
import org.cacrowd.casim.pedca.engine.CAAgentMover;
import org.cacrowd.casim.utility.NullObserver;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.utility.rasterizer.Rasterizer;
import org.cacrowd.casim.utility.rasterizer.ScanlineRasterizer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class TestHybridSim {

    private static GRPCServer myServer = null;

    @AfterClass
    public static void cleanup() {
        if (myServer != null) {
            myServer.stop();
        }
    }

    @Before
    public void initServer() throws Exception {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AgentMover.class).to(CAAgentMover.class);
                bind(SimulationObserver.class).to(NullObserver.class);
                bind(HybridTransitionHandler.class).to(SimpleHybridTransitionHandler.class);
                bind(Rasterizer.class).to(ScanlineRasterizer.class);
            }
        });

        GRPCServer server = injector.getInstance(GRPCServer.class);

        new Thread(() -> {
            try {
                server.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        myServer = server;
    }

    @After
    public void shutdown() {
        myServer.stop();
        myServer = null;
    }

    @Test
    public void testHybridSim() {
        HybridsimTestClient.main(null);
    }
}

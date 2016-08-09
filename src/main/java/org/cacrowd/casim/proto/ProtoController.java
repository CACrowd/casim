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

package org.cacrowd.casim.proto;


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.visualizer.debugger.eventsbaseddebugger.EventBasedVisDebuggerEngine;
import org.cacrowd.casim.proto.engine.CAEngine;
import org.cacrowd.casim.proto.eventshandling.AllEventsHandler;
import org.cacrowd.casim.proto.grpc.CAServer;
import org.cacrowd.casim.proto.scenario.ProtoCAScenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsManagerImpl;

/**
 * Created by laemmel on 20/05/16.
 */
public class ProtoController {

    private static EventBasedVisDebuggerEngine dbg;


    public ProtoController() {


        ProtoCAScenario protoCAScenario = new ProtoCAScenario();
        EventsManager em = new EventsManagerImpl();
        em.addHandler(new AllEventsHandler());

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ProtoCAScenario.class).toInstance(protoCAScenario);
                bind(CAEngine.class).toInstance(new CAEngine());
                bind(EventsManager.class).toInstance(em);
            }
        });


        CAServer server = injector.getInstance(CAServer.class);

        try {
            server.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Constants.VIS) {
            //      			dbg = new EventBasedVisDebuggerEngine(caScenario);
            //      			InfoBox iBox = new InfoBox(dbg, scenario);
            //      			dbg.addAdditionalDrawer(iBox);
            //      			controller.getEvents().addHandler(dbg);
        }
    }

    public static void main(String[] args) {

        Logger.getRootLogger().setLevel(Level.INFO);

        new ProtoController();


    }
}

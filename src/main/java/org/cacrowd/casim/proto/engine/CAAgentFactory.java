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

package org.cacrowd.casim.proto.engine;


import org.apache.log4j.Logger;
import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.proto.HybridSimProto;
import org.cacrowd.casim.proto.agents.PedestrianProto;

/**
 * Created by laemmel on 11/08/16.
 */
public class CAAgentFactory {


    private static final Logger log = Logger.getLogger(CAAgentFactory.class);
    private final Context context;


    public CAAgentFactory(Context context) {
        this.context = context;
    }

    public void createOne(HybridSimProto.Agent request) {
        int intId = request.getId();

//        request.get
        int destinationId = 0;//TODO
        TransitionArea ta = getTransitionArea();
        GridPoint gp = ta.calculateEnterPosition();
        Destination destination = context.getMarkerConfiguration().getDestination(destinationId);

        PedestrianProto ped = new PedestrianProto(intId, gp, destination, context, request.getLeg(), request.getLeaveLocation());
        context.getPopulation().addPedestrian(ped);

        log.info(request);


    }

    private TransitionArea getTransitionArea() {
        return null;//TODO
    }
}

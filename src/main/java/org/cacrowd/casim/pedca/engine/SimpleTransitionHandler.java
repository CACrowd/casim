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

package org.cacrowd.casim.pedca.engine;

import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.agents.PhysicalObject;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridCell;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;

import java.util.LinkedList;
import java.util.Queue;

public class SimpleTransitionHandler implements TransitionHandler {

    private static final Logger log = Logger.getLogger(SimpleAreaTransitionHandler.class);

    private final Queue<Agent> scheduledForDeparture = new LinkedList<>();
    //    private final Queue<Agent> scheduledForArrival = new LinkedList<>();
    Context context;

    public SimpleTransitionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void step(double time) {
        context.getPopulation().getPedestriansMap().entrySet().removeIf(a -> a.getValue().isAboutToLeave() && a.getValue().delete());

        while (scheduledForDeparture.peek() != null) {
            Agent peek = scheduledForDeparture.peek();
            GridPoint pos = peek.getPosition();

            GridCell<PhysicalObject> gp = context.getPedestrianGrid().get(pos);

            if (gp.size() == 0) {
                if (context.getPopulation().addPedestrian(peek)) {
                    context.getPedestrianGrid().addPedestrian(pos, peek);
                }
                log.warn("Agent with ID: " + peek.getID() + " already exist. Agent will not added to simulation.");
                scheduledForDeparture.poll();
            } else {
                break;
            }
        }
    }

    @Override
    public void scheduleForDeparture(Agent a) {
        this.scheduledForDeparture.add(a);
    }

//    @Override
//    public void scheduleForArrival(Agent a) {
////        this.scheduledForArrival.add(a);
//
//    }

    @Override
    public void init() {

    }


}

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

import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.agents.PhysicalObject;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridCell;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;

import java.util.LinkedList;
import java.util.Queue;

public class SimpleTransistionHandler implements TransitionHandler {

    private final Queue<Agent> scheduled = new LinkedList<>();
    Context context;

    public SimpleTransistionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void step(double time) {
        context.getPopulation().getPedestrians().removeIf(a -> a.isArrived() && a.delete());

        while (scheduled.peek() != null) {
            Agent peek = scheduled.peek();
            GridPoint pos = peek.getPosition();

            GridCell<PhysicalObject> gp = context.getPedestrianGrid().get(pos);

            if (gp.size() == 0) {
                context.getPopulation().addPedestrian(peek);
                context.getPedestrianGrid().addPedestrian(pos, peek);
                scheduled.poll();
            } else {
                break;
            }
        }
    }

    @Override
    public void scheduleForDeparture(Agent a) {
        this.scheduled.add(a);
    }


}

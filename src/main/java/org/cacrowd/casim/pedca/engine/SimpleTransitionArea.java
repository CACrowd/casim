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

import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.utility.CASimRandom;

import java.util.*;

public class SimpleTransitionArea implements TransitionArea {
    private final Context context;
    public List<GridPoint> pointList = new ArrayList<>();

    private Queue<Agent> scheduledDepartures = new LinkedList<>();


    public SimpleTransitionArea(Context context) {
        this.context = context;
    }

    @Override
    public void scheduleDeparture(Agent a) {
        scheduledDepartures.add(a);
    }

//    @Override
//    public void scheduleArrival(Agent a) {
//        scheduledArrivals.add(a);
//    }

    @Override
    public void step(double time) {

//        while (scheduledArrivals.peek() != null) {
//            Agent cand = scheduledArrivals.peek();
//            if (CASimRandom.nextDouble() < 0.1) { //mx arrival transition rate
//                context.getPedestrianGrid().removePedestrian(cand.getPosition(), cand);
//                context.getPopulation().remove(cand);
//            }
//        }


        int cnt = 0;
        while (scheduledDepartures.peek() != null) {
            if (CASimRandom.nextDouble() > 0.8) { //mx departure transition rate
                break;
            }
            Agent cand = scheduledDepartures.peek();
            if (incomming(cand)) {
                scheduledDepartures.poll();
            } else {
                break;
            }
        }
    }

    private boolean incomming(Agent cand) {
        Collections.shuffle(pointList);
        for (GridPoint gp : this.pointList) {
            if (this.context.getPedestrianGrid().get(gp).size() == 0) {

                cand.enterPedestrianGrid(gp);
                context.getPopulation().addPedestrian(cand);
                return true;
            }
        }
        return false;
    }
}

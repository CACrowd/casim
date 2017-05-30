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

package org.cacrowd.casim.hybridsim.engine;

import org.cacrowd.casim.pedca.agents.Agent;

import java.util.List;

public class SimpleHybridTransitionHandler implements HybridTransitionHandler {
    @Override
    public void step(double time) {

    }

    @Override
    public void scheduleForDeparture(Agent a) {

    }

    @Override
    public void scheduleForArrival(Agent a) {

    }

    @Override
    public void init() {

    }

    @Override
    public List<Agent> retrieveArrivedAgents() {
        return null;
    }
}

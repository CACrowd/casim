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

import com.google.inject.Inject;
import org.cacrowd.casim.pedca.agents.ActivePopulation;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;

public class AgentsUpdater {
    private ActivePopulation population;

    @Inject
    public AgentsUpdater(Context context) {
        this.population = context.getPopulation();
    }

    public void step() {
        if (!population.isEmpty()) {
            for (Agent pedestrian : population.getPedestrians()) {
                //TODO maybe here is not the right place for this check
                if (!pedestrian.isArrived()) {
                    pedestrian.updateChoice();
                }
            }
        }
    }
}

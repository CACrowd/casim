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

package org.cacrowd.casim.pedca.agents;

import java.util.ArrayList;

public class Population {
    private ArrayList<Agent> pedestrians;

    public Population() {
        pedestrians = new ArrayList<Agent>();
    }

    public void addPedestrian(Agent pedestrian) {
        pedestrians.add(pedestrian);
    }

    public void remove(Agent pedestrian) {
        pedestrians.remove(pedestrian);
    }

    public Agent getPedestrian(int index) {
        return pedestrians.get(index);
    }

    public ArrayList<Agent> getPedestrians() {
        return pedestrians;
    }

    public int size() {
        return pedestrians.size();
    }

    public boolean isEmpty() {
        return pedestrians.size() == 0;
    }
}

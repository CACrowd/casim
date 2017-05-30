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

package org.cacrowd.casim.pedca.agents;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ActivePopulation {
    private Map<Integer, Agent> pedestrians;

    public ActivePopulation() {
        pedestrians = new LinkedHashMap<>();
    }

    public boolean addPedestrian(Agent a) {
        if (pedestrians.containsKey(a.getID())) {
            return false;
        }
        pedestrians.put(a.getID(), a);
        return true;
    }

    public void remove(Agent a) {
        pedestrians.remove(a.getID());
    }

//    public Agent getPedestrian(int index) {
//        return pedestrians.get(index);
//    }

    public Collection<Agent> getPedestrians() {
        return pedestrians.values();
    }

    public Map<Integer, Agent> getPedestriansMap() {
        return pedestrians;
    }

    public int size() {
        return pedestrians.size();
    }

    public boolean isEmpty() {
        return pedestrians.size() == 0;
    }

    public Agent remove(int id) {
        return pedestrians.remove(id);
    }
}

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

package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarkerConfigurationImpl implements MarkerConfiguration {
    private final Map<Integer, Destination> destinationMap = new HashMap<>();
    private ArrayList<Start> starts;
    private ArrayList<Destination> destinations;
    private ArrayList<GridPoint> destinationsCells;

    public MarkerConfigurationImpl() {
        starts = new ArrayList<>();
        destinations = new ArrayList<>();
        destinationsCells = new ArrayList<>();
    }

    //Constructor never used
//	public MarkerConfiguration(ArrayList<Start> starts, ArrayList<Destination> destinations){
//		this.starts = starts;
//		this.destinations = destinations;
//	}


    //TODO tests
    @Override
    public Destination getTacticalDestination(int destinationID) {
        return destinations.get(destinationID);
    }

    @Override
    public void addTacticalDestination(Destination destination) {
        destinationMap.put(destination.getId(), destination);
        destinations.add(destination);
        destinationsCells.addAll(destination.getCells());
    }

    @Override
    public void addStart(Start start) {
        starts.add(start);
    }

    @Override
    public ArrayList<Start> getStarts() {
        return starts;
    }

    @Override
    public ArrayList<Destination> getTacticalDestinations() {
        return destinations;
    }

    @Override
    public Destination getDestination(int id) {
        return this.destinationMap.get(id);
    }


    //Method never used
//	public ArrayList<GridPoint> getBorderCells() {
//		return destinationsCells;
//	}
}

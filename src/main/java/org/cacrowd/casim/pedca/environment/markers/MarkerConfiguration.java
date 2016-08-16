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

package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;

import java.util.ArrayList;

public class MarkerConfiguration {
	private ArrayList<Start> starts;
	private ArrayList<Destination> destinations;
	private ArrayList<GridPoint> destinationsCells;
	
	public MarkerConfiguration(){
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
	public Destination getDestination(int destinationID){
		return destinations.get(destinationID);
	}
	
	public void addDestination(Destination destination){
		destinations.add(destination);
		destinationsCells.addAll(destination.getCells());
	}
	
	public void addStart(Start start){
		starts.add(start);
	}

    public ArrayList<Start> getStarts(){
		return starts;
	}

    public ArrayList<Destination> getDestinations(){
		return destinations;
	}


    //Method never used
//	public ArrayList<GridPoint> getBorderCells() {
//		return destinationsCells;
//	}
}

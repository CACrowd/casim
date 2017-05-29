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


import java.util.ArrayList;

/**
 * Created by laemmel on 16/08/16.
 */
public interface MarkerConfiguration {
    //TODO tests
    //TODO strategic destination aka transition area [GL May '17]
    Destination getTacticalDestination(int destinationID);

    void addTacticalDestination(Destination destination);

    ArrayList<Destination> getTacticalDestinations();
    
    void addStart(Start start);

    ArrayList<Start> getStarts();

    Destination getDestination(int id);
}

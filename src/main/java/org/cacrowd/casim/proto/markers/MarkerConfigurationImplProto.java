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

package org.cacrowd.casim.proto.markers;

import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.environment.markers.Start;

import java.util.ArrayList;

public class MarkerConfigurationImplProto implements MarkerConfiguration {

    @Override
    public Destination getDestination(int destinationID) {
        return null;
    }

    @Override
    public void addDestination(Destination destination) {

    }

    @Override
    public void addStart(Start start) {

    }

    @Override
    public ArrayList<Start> getStarts() {
        return null;
    }

    @Override
    public ArrayList<Destination> getDestinations() {
        return null;
    }
}

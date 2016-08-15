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

package org.cacrowd.casim.proto.agents;

import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.proto.HybridSimProto;

/**
 * Created by laemmel on 15/08/16.
 */
public class PedestrianProto extends Agent {
    public PedestrianProto(int Id, GridPoint position, Destination destination, Context context, HybridSimProto.Leg leg, HybridSimProto.Coordinate leaveLocation) {
        super(Id, position, destination, context);
    }
}

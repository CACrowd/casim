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

package org.cacrowd.casim.proto.engine;
/****************************************************************************/
/****************************************************************************/
//
//   This file is part of casim.
//   casim is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 2 of the License, or
//   (at your option) any later version.
//
/****************************************************************************/

import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.AgentMover;

/**
 * Created by laemmel on 09/08/16.
 */
public class CAAgentMoverProto implements AgentMover {
    public CAAgentMoverProto(CAEngine caEngine, Context context, CAEnvironment env) {
    }

    @Override
    public void step(double time) {
        throw new RuntimeException("Implement me!");
    }
}

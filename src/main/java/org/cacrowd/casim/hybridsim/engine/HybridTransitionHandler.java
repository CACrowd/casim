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

import org.cacrowd.casim.pedca.engine.TransitionHandler;
import org.cacrowd.casim.proto.HybridSimProto;

/**
 * Created by laemmel on 30.05.17.
 */
public interface HybridTransitionHandler extends TransitionHandler {

    HybridSimProto.Agents queryRetrievableAgents();

    void confirmRetrievedAgents(HybridSimProto.Agents confirmed);
}

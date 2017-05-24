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

import com.google.inject.Inject;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.*;
import org.cacrowd.casim.utility.SimulationObserver;

public class HybridSimulationEngine {

    @Inject
    private AgentsGenerator agentGenerator;
    @Inject
    private AgentsUpdater agentUpdater;
    @Inject
    private ConflictSolver conflictSolver;
    @Inject
    private AgentMover agentMover;
    @Inject
    private Context context;
    @Inject
    private GridsAndObjectsUpdater activeObjectsUpdater;
    @Inject
    private SimulationObserver observer;
    @Inject
    private TransitionHandler transitionHandler;

//    private final Rasterizer rasterizer = new Rasterizer();


}

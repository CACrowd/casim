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

import org.cacrowd.casim.pedca.environment.grid.GridPoint;

/**
 * Created by laemmel on 17.05.17.
 */

//tactical level
public interface Tactic {
    double getStaticFFValue(GridPoint gridPoint);

    boolean exit(GridPoint position);
}

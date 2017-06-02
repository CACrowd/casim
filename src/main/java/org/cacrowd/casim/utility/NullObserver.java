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

package org.cacrowd.casim.utility;

public class NullObserver implements SimulationObserver {
    @Override
    public void observerEnvironmentGrid() {
        //intentionally empty
    }

    @Override
    public void observerDensityGrid() {
        //intentionally empty
    }

    @Override
    public void observePopulation() {
        //intentionally empty
    }


//    @Override
//    public void observeTransitionAreas(Map<GridPoint, TransitionArea> areaMap) {
//        //intentionally empty
//    }
}

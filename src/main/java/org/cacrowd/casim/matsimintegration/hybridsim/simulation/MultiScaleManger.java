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

package org.cacrowd.casim.matsimintegration.hybridsim.simulation;


import com.google.inject.Singleton;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MultiScaleManger implements AfterMobsimListener {


    private List<MultiScaleProvider> multiScaleProviders = new ArrayList<>();


    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {
        multiScaleProviders.forEach(p -> p.setRunCAIteration(event.getIteration() % 2 != 0));
    }

    public void subscribe(MultiScaleProvider p) {
        this.multiScaleProviders.add(p);
    }
}

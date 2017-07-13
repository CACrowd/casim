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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.mobsim.qsim.qnetsimengine.HybridNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.MyQNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;

public class MultiScaleNetworkProvider implements Provider<QNetworkFactory>, IterationStartsListener {

    private boolean evenIt = false;


    private HybridNetworkFactory hybridNetworkFactory;

    private MyQNetworkFactory defaultQNetworkFactory;

    @Inject
    public MultiScaleNetworkProvider(Injector injector) {
        this.hybridNetworkFactory = injector.getInstance(HybridNetworkFactory.class);
        this.defaultQNetworkFactory = new MyQNetworkFactory(injector.getInstance(EventsManager.class), injector.getInstance(Scenario.class));

    }

    @Override
    public QNetworkFactory get() {
        if (evenIt) {
            return hybridNetworkFactory;
        } else {
            return defaultQNetworkFactory;
        }
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent iterationStartsEvent) {
        evenIt = iterationStartsEvent.getIteration() % 10 == 0;
    }
}

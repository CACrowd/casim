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

import com.google.inject.*;
import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.framework.listeners.MobsimListener;
import org.matsim.core.mobsim.qsim.*;
import org.matsim.core.mobsim.qsim.changeeventsengine.NetworkChangeEventsPlugin;
import org.matsim.core.mobsim.qsim.interfaces.ActivityHandler;
import org.matsim.core.mobsim.qsim.interfaces.DepartureHandler;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.messagequeueengine.MessageQueuePlugin;
import org.matsim.core.mobsim.qsim.pt.TransitEnginePlugin;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEnginePlugin;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;

import java.util.ArrayList;
import java.util.Collection;

@Singleton
public class MultiScaleMobsimProvider implements Provider<Mobsim>, MultiScaleProvider {

    private final Controler controller;
    private final IdIntMapper idIntMapper;
    private final GRPCExternalClient grpcExternalClient;
    private final Injector injector;
    private boolean runCA = true;

    @Inject
    public MultiScaleMobsimProvider(Controler controller, IdIntMapper idIntMapper, GRPCExternalClient grpcExternalClient, Injector injector, MultiScaleManger multiScaleManger) {
        this.controller = controller;
        this.idIntMapper = idIntMapper;
        this.grpcExternalClient = grpcExternalClient;
        this.injector = injector;
        multiScaleManger.subscribe(this);
    }

    @Override
    public Mobsim get() {

        if (runCA) {
            HybridMobsimProvider provider = new HybridMobsimProvider(this.injector.getInstance(QNetworkFactory.class), idIntMapper, grpcExternalClient, controller);
            return provider.get();
        } else {
            return getQSim();
        }

    }

    private QSim getQSim() {

        Collection<AbstractQSimPlugin> plugins = getQSimPlugins(controller.getConfig());

        com.google.inject.AbstractModule module = new com.google.inject.AbstractModule() {
            @Override
            protected void configure() {
                for (AbstractQSimPlugin plugin : plugins) {
                    for (Module module1 : plugin.modules()) {
                        install(module1);
                    }
                }
                bind(QSim.class).asEagerSingleton();
                bind(Netsim.class).to(QSim.class);
            }
        };
        Injector qSimLocalInjector = injector.createChildInjector(module);
        QSim qSim = qSimLocalInjector.getInstance(QSim.class);
        for (AbstractQSimPlugin plugin : plugins) {
            for (Class<? extends MobsimEngine> mobsimEngine : plugin.engines()) {
                qSim.addMobsimEngine(qSimLocalInjector.getInstance(mobsimEngine));
            }
            for (Class<? extends ActivityHandler> activityHandler : plugin.activityHandlers()) {
                qSim.addActivityHandler(qSimLocalInjector.getInstance(activityHandler));
            }
            for (Class<? extends DepartureHandler> mobsimEngine : plugin.departureHandlers()) {
                qSim.addDepartureHandler(qSimLocalInjector.getInstance(mobsimEngine));
            }
            for (Class<? extends MobsimListener> mobsimListener : plugin.listeners()) {
                qSim.addQueueSimulationListeners(qSimLocalInjector.getInstance(mobsimListener));
            }
            for (Class<? extends AgentSource> agentSource : plugin.agentSources()) {
                qSim.addAgentSource(qSimLocalInjector.getInstance(agentSource));
            }
        }
        return qSim;
    }

    private Collection<AbstractQSimPlugin> getQSimPlugins(Config config1) {
        final Collection<AbstractQSimPlugin> plugins = new ArrayList<>();
        plugins.add(new MessageQueuePlugin(config1));
        plugins.add(new ActivityEnginePlugin(config1));
        plugins.add(new QNetsimEnginePlugin(config1));
        if (config1.network().isTimeVariantNetwork()) {
            plugins.add(new NetworkChangeEventsPlugin(config1));
        }
        if (config1.transit().isUseTransit()) {
            plugins.add(new TransitEnginePlugin(config1));
        }
        plugins.add(new TeleportationPlugin(config1));
        plugins.add(new PopulationPlugin(config1));
        return plugins;
    }

    @Override
    public void setRunCAIteration(boolean runCA) {
        this.runCA = runCA;
    }
}

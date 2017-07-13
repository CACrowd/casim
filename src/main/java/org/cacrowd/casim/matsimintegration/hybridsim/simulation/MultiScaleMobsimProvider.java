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
import com.google.inject.Provider;
import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.matsim.core.controler.Controler;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.HybridNetworkFactory;

public class MultiScaleMobsimProvider implements Provider<Mobsim> {
    private static final boolean CASIM = true;
    private final Controler controller;
    private final HybridNetworkFactory networkFactory;
    private final IdIntMapper idIntMapper;
    private final GRPCExternalClient grpcExternalClient;

    @Inject
    public MultiScaleMobsimProvider(Controler controller, HybridNetworkFactory networkFactory, IdIntMapper idIntMapper, GRPCExternalClient grpcExternalClient) {
        this.controller = controller;
        this.networkFactory = networkFactory;
        this.idIntMapper = idIntMapper;
        this.grpcExternalClient = grpcExternalClient;
    }

    @Override
    public Mobsim get() {

        if (CASIM) {
            HybridMobsimProvider provider = new HybridMobsimProvider(networkFactory, idIntMapper, grpcExternalClient, controller);
            return provider.get();
        } else {
            return null;
        }

    }

//    public QSim getQSim() {
//        AbstractModule module = new AbstractModule() {
//			@Override
//			protected void configure() {
//				for (AbstractQSimPlugin plugin : plugins) {
//					for (Module module1 : plugin.modules()) {
//						install(module1);
//					}
//				}
//				bind(QSim.class).asEagerSingleton();
//				bind(Netsim.class).to(QSim.class);
//			}
//		};
//        Injector qSimLocalInjector = injector.createChildInjector(module);
//        QSim qSim = qSimLocalInjector.getInstance(QSim.class);
//        for (AbstractQSimPlugin plugin : plugins) {
//			for (Class<? extends MobsimEngine> mobsimEngine : plugin.engines()) {
//				qSim.addMobsimEngine(qSimLocalInjector.getInstance(mobsimEngine));
//			}
//			for (Class<? extends ActivityHandler> activityHandler : plugin.activityHandlers()) {
//				qSim.addActivityHandler(qSimLocalInjector.getInstance(activityHandler));
//			}
//			for (Class<? extends DepartureHandler> mobsimEngine : plugin.departureHandlers()) {
//				qSim.addDepartureHandler(qSimLocalInjector.getInstance(mobsimEngine));
//			}
//			for (Class<? extends MobsimListener> mobsimListener : plugin.listeners()) {
//				qSim.addQueueSimulationListeners(qSimLocalInjector.getInstance(mobsimListener));
//			}
//			for (Class<? extends AgentSource> agentSource : plugin.agentSources()) {
//				qSim.addAgentSource(qSimLocalInjector.getInstance(agentSource));
//			}
//		}
//        return qSim;
//    }
}

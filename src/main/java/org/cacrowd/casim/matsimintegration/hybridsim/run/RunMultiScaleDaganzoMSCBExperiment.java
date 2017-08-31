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

package org.cacrowd.casim.matsimintegration.hybridsim.run;

import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.matsimintegration.hybridsim.mscb.MSCBCongestionObserver;
import org.cacrowd.casim.matsimintegration.hybridsim.mscb.MSCBTravelDisutility;
import org.cacrowd.casim.matsimintegration.hybridsim.mscb.MSCBTravelDisutilityFactory;
import org.cacrowd.casim.matsimintegration.hybridsim.simulation.MultiScaleManger;
import org.cacrowd.casim.matsimintegration.hybridsim.simulation.MultiScaleMobsimProvider;
import org.cacrowd.casim.matsimintegration.hybridsim.simulation.MultiScaleNetworkProvider;
import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.cacrowd.casim.matsimintegration.scenarios.DaganzoExperimentRunInfoSender;
import org.cacrowd.casim.matsimintegration.scenarios.DaganzoScenarioGernator;
import org.cacrowd.casim.proto.HybridSimProto;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.HybridNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.IOException;

public class RunMultiScaleDaganzoMSCBExperiment {
    public static void run(double bottleneckWidth) throws IOException, InterruptedException {

        Config c = ConfigUtils.createConfig();
        c.network().setTimeVariantNetwork(true);

        c.controler().setLastIteration(20);
        c.controler().setWriteEventsInterval(1);

        c.qsim().setEndTime(3600);

        final IdIntMapper idIntMapper = new IdIntMapper();
        final Scenario sc = ScenarioUtils.createScenario(c);
        HybridSimProto.Scenario hsc = DaganzoScenarioGernator.generateScenario(sc, idIntMapper, bottleneckWidth);

        GRPCExternalClient client = new GRPCExternalClient("localhost", 9000);
        client.getBlockingStub().initScenario(hsc);

        final Controler controller = new Controler(sc);
        controller.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

        final EventsManager eventsManager = EventsUtils.createEventsManager();

        final MSCBTravelDisutility tc = new MSCBTravelDisutility();
        final MSCBCongestionObserver obs = new MSCBCongestionObserver();


        controller.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {
                addEventHandlerBinding().toInstance(tc);
                addEventHandlerBinding().toInstance(obs);
                addMobsimListenerBinding().toInstance(obs);
                bind(MSCBTravelDisutility.class).toInstance(tc);
                addControlerListenerBinding().toInstance(tc);
                bindCarTravelDisutilityFactory().to(MSCBTravelDisutilityFactory.class);
                bind(HybridNetworkFactory.class).toInstance(new HybridNetworkFactory());
                bind(QNetworkFactory.class).toProvider(MultiScaleNetworkProvider.class);
                bind(IdIntMapper.class).toInstance(idIntMapper);
                bind(GRPCExternalClient.class).toInstance(client);
                bindEventsManager().toInstance(eventsManager);
                bind(Controler.class).toInstance(controller);
                addControlerListenerBinding().toProvider(() -> new DaganzoExperimentRunInfoSender(client, bottleneckWidth, "Nash approach"));
                bind(Mobsim.class).toProvider(MultiScaleMobsimProvider.class);
//                bind(MultiScaleManger.class).toInstance(manager);
                addControlerListenerBinding().to(MultiScaleManger.class);

            }

        });

        controller.run();
        client.getBlockingStub().shutdown(HybridSimProto.Empty.getDefaultInstance());
        client.shutdown();

    }
}

/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.cacrowd.casim.matsimintegration.hybridsim.run;

import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.matsimintegration.hybridsim.simulation.HybridMobsimProvider;
import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.cacrowd.casim.matsimintegration.scenarios.DaganzoExperimentRunInfoSender;
import org.cacrowd.casim.matsimintegration.scenarios.DiamondScenarioGenerator;
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


// Runs diamond-setup experiments
public class RunDiamondNashExperiment {

    public static void run(double scWidth, double scHeight, double passagesWidth) throws IOException, InterruptedException {

        Config c = ConfigUtils.createConfig();
        c.controler().setLastIteration(20);
        c.controler().setWriteEventsInterval(1);

        c.qsim().setEndTime(3600);

        final IdIntMapper idIntMapper = new IdIntMapper();
        final Scenario sc = ScenarioUtils.createScenario(c);
        HybridSimProto.Scenario hsc = DiamondScenarioGenerator.generateScenario(sc, idIntMapper, scWidth, scHeight, passagesWidth);

        GRPCExternalClient client = new GRPCExternalClient("localhost", 9000);
        client.getBlockingStub().initScenario(hsc);

        final Controler controller = new Controler(sc);
        controller.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

        final EventsManager eventsManager = EventsUtils.createEventsManager();


        controller.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {
                bindEventsManager().toInstance(eventsManager);
                addControlerListenerBinding().toProvider(() -> new DaganzoExperimentRunInfoSender(client, passagesWidth, "Nash approach"));
                bind(Mobsim.class).toProvider(HybridMobsimProvider.class);
                bind(HybridNetworkFactory.class).toInstance(new HybridNetworkFactory());
                bind(QNetworkFactory.class).to(HybridNetworkFactory.class);
                bind(IdIntMapper.class).toInstance(idIntMapper);
                bind(GRPCExternalClient.class).toInstance(client);
                bind(Controler.class).toInstance(controller);
            }
        });

        controller.run();
        client.getBlockingStub().shutdown(HybridSimProto.Empty.getDefaultInstance());
        client.shutdown();

    }


}

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

package org.cacrowd.casim.matsimintegration.hybridsim.mscb;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.matsimintegration.hybridsim.events.RunInfoSender;
import org.cacrowd.casim.matsimintegration.hybridsim.simulation.HybridMobsimProvider;
import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.cacrowd.casim.proto.HybridSimProto;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.HybridNetworkFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetworkFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by laemmel on 09.03.16.
 */
public class RunMSCBDaganzoExample {


    private static final Logger log = Logger.getLogger(RunMSCBDaganzoExample.class);
    private static final IdIntMapper idIntMapper = new IdIntMapper();
    public static String REMOTE_HOST = "localhost";
    public static int REMOTE_PORT = 9000;

    public static void main(String[] args) throws IOException, InterruptedException {

        if (args != null && args.length == 2) {
            REMOTE_HOST = args[0];
            REMOTE_PORT = Integer.parseInt(args[1]);
        }

        Logger.getRootLogger().setLevel(Level.INFO);//Make output less verbose as netty is so blathering


        Config c = ConfigUtils.createConfig();
        c.controler().setLastIteration(20);
        c.controler().setWriteEventsInterval(1);

        c.qsim().setEndTime(3600);


        final Scenario sc = ScenarioUtils.createScenario(c);
        enrichConfig(c);
        createNetwork(sc);
        createPopulation(sc);
        HybridSimProto.Scenario hsc = createScenario();

        GRPCExternalClient client = new GRPCExternalClient("localhost", 9000);
        client.getBlockingStub().initScenario(hsc);

        final Controler controller = new Controler(sc);
        controller.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

        final EventsManager eventsManager = EventsUtils.createEventsManager();

        final MSCBTravelDisutility tc = new MSCBTravelDisutility();
        final MSCBCongestionObserver obs = new MSCBCongestionObserver();

        Injector mobsimProviderInjector = Guice.createInjector(new com.google.inject.AbstractModule() {
            @Override
            protected void configure() {
                bind(Scenario.class).toInstance(sc);
                bind(EventsManager.class).toInstance(eventsManager);
                bind(HybridNetworkFactory.class).toInstance(new HybridNetworkFactory());
                bind(QNetworkFactory.class).to(HybridNetworkFactory.class);
                bind(IdIntMapper.class).toInstance(idIntMapper);
                bind(GRPCExternalClient.class).toInstance(client);
            }

        });

        controller.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {
                addEventHandlerBinding().toInstance(tc);
                addEventHandlerBinding().toInstance(obs);
                addMobsimListenerBinding().toInstance(obs);
                bind(MSCBTravelDisutility.class).toInstance(tc);
                addControlerListenerBinding().toInstance(tc);
                bindCarTravelDisutilityFactory().to(MSCBTravelDisutilityFactory.class);
                bindEventsManager().toInstance(eventsManager);
                addControlerListenerBinding().toProvider(new Provider<IterationStartsListener>() {
                    @Override
                    public IterationStartsListener get() {
                        return new RunInfoSender(client);
                    }
                });
                bind(Mobsim.class).toProvider(new Provider<Mobsim>() {
                    @Override
                    public Mobsim get() {
                        HybridMobsimProvider provider = mobsimProviderInjector.getInstance(HybridMobsimProvider.class);
                        return provider.get(controller);
                    }
                });
            }
        });


        controller.run();

        client.getBlockingStub().shutdown(HybridSimProto.Empty.getDefaultInstance());
        client.shutdown();


    }


    private static HybridSimProto.Scenario createScenario() {

        HybridSimProto.Scenario.Builder sb = HybridSimProto.Scenario.newBuilder();
        HybridSimProto.Edge.Builder eb = HybridSimProto.Edge.newBuilder();
        HybridSimProto.Coordinate.Builder cb = HybridSimProto.Coordinate.newBuilder();

        cb.setX(0);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(17.3);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());


        eb.setC0(cb.build());
        cb.setX(23.2);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        eb.setC0(cb.build());
        cb.setY(0.);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        eb.setC0(cb.build());
        cb.setX(0.);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());


        //BEGIN bottleneck

        cb.setX(8.4);
        cb.setY(16.9);
        eb.setC0(cb.build());
        cb.setX(14.8);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

//        cb.setX(8.4);
//        cb.setY(16.5);
//        eb.setC0(cb.build());
//        cb.setX(14.8);
//        eb.setC1(cb.build());
//        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
//        sb.addEdges(eb.build());


        cb.setX(8.4);
        cb.setY(15.7);
        eb.setC0(cb.build());
        cb.setX(14.8);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(8.4);
        cb.setY(15.3);
        eb.setC0(cb.build());
        cb.setX(14.8);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        //END bottleneck

        cb.setX(0);
        cb.setY(14.9);
        eb.setC0(cb.build());
        cb.setX(3.2);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(5.7);
        eb.setC0(cb.build());
        cb.setX(17);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setY(14.5);
        cb.setX(5.7);
        eb.setC0(cb.build());
        cb.setX(17.4);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(.5);
        cb.setY(15.3);
        eb.setC0(cb.build());
        cb.setY(16.8);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(6);
        sb.addEdges(eb.build());

        cb.setX(3.2);
        cb.setY(15.3);
        eb.setC0(cb.build());
        cb.setY(16.7);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(7);
        sb.addEdges(eb.build());

        cb.setX(3.6);
        cb.setY(14.8);
        eb.setC0(cb.build());
        cb.setX(5.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(4);
        sb.addEdges(eb.build());

        cb.setX(0);
        cb.setY(14.5);
        eb.setC0(cb.build());
        cb.setX(3.2);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(5.7);
        cb.setY(15.3);
        eb.setC0(cb.build());
        cb.setY(16.7);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(8);
        sb.addEdges(eb.build());

        cb.setX(3.5);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(14.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(.5);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(14.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(5.7);
        cb.setY(2.5);
        eb.setC0(cb.build());
        cb.setY(14.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        //        cb.setX(6.1);
        //        cb.setY(2.5);
        //        eb.setC0(cb.build());
        //        cb.setY(14.6);
        //        eb.setC1(cb.build());
        //        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        //        sb.addEdges(eb.build());

        cb.setX(5.7);
        cb.setY(2.5);
        eb.setC0(cb.build());
        cb.setX(17.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());


        cb.setX(5.7);
        cb.setY(2.3);
        eb.setC0(cb.build());
        cb.setY(0.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(0);
        sb.addEdges(eb.build());

        cb.setX(5.2);
        cb.setY(2.5);
        eb.setC0(cb.build());
        cb.setX(3.8);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(2);
        sb.addEdges(eb.build());

        cb.setX(17.6);
        cb.setY(2.5);
        eb.setC0(cb.build());
        cb.setY(14.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(17.6);
        cb.setY(2.3);
        eb.setC0(cb.build());
        cb.setY(0.5);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(1);
        sb.addEdges(eb.build());

        cb.setX(18);
        cb.setY(2.7);
        eb.setC0(cb.build());
        cb.setX(19.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(3);
        sb.addEdges(eb.build());

        cb.setX(20.);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(15);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(18);
        cb.setY(14.9);
        eb.setC0(cb.build());
        cb.setX(19.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(5);
        sb.addEdges(eb.build());

        cb.setX(14.8);
        cb.setY(14.9);
        eb.setC0(cb.build());
        cb.setX(17.6);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(20.);
        cb.setY(15);
        eb.setC0(cb.build());
        cb.setX(23.2);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(17.6);
        cb.setY(15.4);
        eb.setC0(cb.build());
        cb.setY(16.9);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(9);
        sb.addEdges(eb.build());

        cb.setX(20);
        cb.setY(15.4);
        eb.setC0(cb.build());
        cb.setY(16.9);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(10);
        sb.addEdges(eb.build());

        cb.setX(22.5);
        cb.setY(15.4);
        eb.setC0(cb.build());
        cb.setY(16.9);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(11);
        sb.addEdges(eb.build());

        return sb.build();
    }

    private static void enrichConfig(Config c) {
        PlanCalcScoreConfigGroup.ActivityParams pre = new PlanCalcScoreConfigGroup.ActivityParams("origin");

        c.strategy().setMaxAgentPlanMemorySize(3);
        c.strategy().addParam("ModuleDisableAfterIteration_1", "10");
        c.strategy().addParam("Module_1", "ReRoute");
        c.strategy().addParam("ModuleProbability_1", "0.1");
        c.strategy().addParam("Module_2", "ChangeExpBeta");
        c.strategy().addParam("ModuleProbability_2", "0.9");

        c.travelTimeCalculator().setTravelTimeCalculatorType("TravelTimeCalculatorHashMap");
//        c.travelTimeCalculator().setTravelTimeAggregatorType("experimental_LastMile");
        c.travelTimeCalculator().setTraveltimeBinSize(60);

        pre.setTypicalDuration(49); // needs to be geq 49, otherwise when
        // running a simulation one gets
        // "java.lang.RuntimeException: zeroUtilityDuration of type pre-evac must be greater than 0.0. Did you forget to specify the typicalDuration?"
        // the reason is the double precision. see also comment in
        // ActivityUtilityParameters.java (gl)
        pre.setMinimalDuration(49);
        pre.setClosingTime(49);
        pre.setEarliestEndTime(49);
        pre.setLatestStartTime(49);
        pre.setOpeningTime(49);

        PlanCalcScoreConfigGroup.ActivityParams post = new PlanCalcScoreConfigGroup.ActivityParams("destination");
        post.setTypicalDuration(49); // dito
        post.setMinimalDuration(49);
        post.setClosingTime(49);
        post.setEarliestEndTime(49);
        post.setLatestStartTime(49);
        post.setOpeningTime(49);
        c.planCalcScore().addActivityParams(pre);
        c.planCalcScore().addActivityParams(post);

        c.planCalcScore().setLateArrival_utils_hr(0.);
        c.planCalcScore().setPerforming_utils_hr(0.);
    }

    private static void createPopulation(Scenario sc) {
        Population pop = sc.getPopulation();
        PopulationFactory fac = pop.getFactory();
        for (int i = 0; i < 500; i++) {
            Person pers = fac.createPerson(Id.createPersonId(i));
            pop.addPerson(pers);
            Plan plan = fac.createPlan();
            pers.addPlan(plan);
            Activity a0 = fac.createActivityFromLinkId("origin", Id.createLinkId("origin"));
            a0.setEndTime(0);
            plan.addActivity(a0);
            Leg leg = fac.createLeg("car");
            plan.addLeg(leg);
            Activity a1 = fac.createActivityFromLinkId("destination", Id.createLinkId("destination"));
            plan.addActivity(a1);
        }
//		for (int i = 20; i < 40; i++) {
//			Person pers = fac.createPerson(Id.createPersonId(i));
//			pop.addPerson(pers);
//			Plan plan = fac.createPlan();
//			pers.addPlan(plan);
//			Activity a0 = fac.createActivityFromLinkId("origin",Id.createLinkId("3r"));
//			a0.setEndTime(i-20);
//			plan.addActivity(a0);
//			Leg leg = fac.createLeg("car");
//			plan.addLeg(leg);
//			Activity a1 = fac.createActivityFromLinkId("destination",Id.createLinkId("0r"));
//			plan.addActivity(a1);
//		}
    }

    private static void createNetwork(Scenario sc) {
        Network net = sc.getNetwork();
        net.setCapacityPeriod(1);
        net.setEffectiveLaneWidth(0.71);
        net.setEffectiveCellSize(0.26);
        NetworkFactory fac = net.getFactory();
//        Node nm2 = fac.createNode(Id.createNodeId(-2), CoordUtils.createCoord(-9.5, 16.2));
//        net.addNode(nm2);
        Node nm1 = fac.createNode(Id.createNodeId(-1), CoordUtils.createCoord(-4.5, 16.2));
        net.addNode(nm1);
        Node n6 = fac.createNode(Id.createNodeId(6), CoordUtils.createCoord(.5, 16.2));
        net.addNode(n6);
        Node n7 = fac.createNode(Id.createNodeId(7), CoordUtils.createCoord(3.2, 16.2));
        net.addNode(n7);
        Node n8 = fac.createNode(Id.createNodeId(8), CoordUtils.createCoord(5.7, 16.2));
        net.addNode(n8);
        Node n9 = fac.createNode(Id.createNodeId(9), CoordUtils.createCoord(17.6, 16.2));
        net.addNode(n9);
        Node n10 = fac.createNode(Id.createNodeId(10), CoordUtils.createCoord(20, 16.2));
        net.addNode(n10);
        Node n11 = fac.createNode(Id.createNodeId(11), CoordUtils.createCoord(22.5, 16.2));
        net.addNode(n11);
        Node nm3 = fac.createNode(Id.createNodeId(-3), CoordUtils.createCoord(27.5, 16.2));
        net.addNode(nm3);
//        Node nm4 = fac.createNode(Id.createNodeId(-4), CoordUtils.createCoord(32.5, 16.2));
//        net.addNode(nm4);
        Node n4 = fac.createNode(Id.createNodeId(4), CoordUtils.createCoord(4.2, 14.8));
        net.addNode(n4);
        Node n5 = fac.createNode(Id.createNodeId(5), CoordUtils.createCoord(18.8, 14.8));
        net.addNode(n5);
        Node n2 = fac.createNode(Id.createNodeId(2), CoordUtils.createCoord(4.2, 2.5));
        net.addNode(n2);
        Node n3 = fac.createNode(Id.createNodeId(3), CoordUtils.createCoord(18.8, 2.5));
        net.addNode(n3);
        Node n0 = fac.createNode(Id.createNodeId(0), CoordUtils.createCoord(5.7, 1.4));
        net.addNode(n0);
        Node n1 = fac.createNode(Id.createNodeId(1), CoordUtils.createCoord(17.6, 1.4));
        net.addNode(n1);


//        Link lm2m1 = fac.createLink(Id.createLinkId("origin"), nm2, nm1);
//        net.addLink(lm2m1);
        Link lm16 = fac.createLink(Id.createLinkId("origin"), nm1, n6);
        net.addLink(lm16);

        Link lin = fac.createLink(Id.createLinkId("in"), n6, n7);
        net.addLink(lin);
        idIntMapper.addDestinationsLinkMapping(6, 7, lin);

        Link l78 = fac.createLink(Id.createLinkId("7->8"), n7, n8);
        net.addLink(l78);
        idIntMapper.addDestinationsLinkMapping(7, 8, l78);

        Link l89 = fac.createLink(Id.createLinkId("8->9"), n8, n9);
        net.addLink(l89);
        idIntMapper.addDestinationsLinkMapping(8, 9, l89);

        Link l910 = fac.createLink(Id.createLinkId("9->10"), n9, n10);
        net.addLink(l910);
        idIntMapper.addDestinationsLinkMapping(9, 10, l910);

        Link lout = fac.createLink(Id.createLinkId("out"), n10, n11);
        net.addLink(lout);
        idIntMapper.addDestinationsLinkMapping(10, 11, lout);

        Link l11m3 = fac.createLink(Id.createLinkId("destination"), n11, nm3);
        net.addLink(l11m3);

//        Link lm3m4 = fac.createLink(Id.createLinkId("m3->m4"), nm3, nm4);
//        net.addLink(lm3m4);

        Link l74 = fac.createLink(Id.createLinkId("7->4"), n7, n4);
        net.addLink(l74);
        idIntMapper.addDestinationsLinkMapping(7, 4, l74);

        Link l42 = fac.createLink(Id.createLinkId("4->2"), n4, n2);
        net.addLink(l42);
        idIntMapper.addDestinationsLinkMapping(4, 2, l42);

        Link l20 = fac.createLink(Id.createLinkId("2->0"), n2, n0);
        net.addLink(l20);
        idIntMapper.addDestinationsLinkMapping(2, 0, l20);

        Link l01 = fac.createLink(Id.createLinkId("0->1"), n0, n1);
        net.addLink(l01);
        idIntMapper.addDestinationsLinkMapping(0, 1, l01);

        Link l13 = fac.createLink(Id.createLinkId("1->3"), n1, n3);
        net.addLink(l13);
        idIntMapper.addDestinationsLinkMapping(1, 3, l13);

        Link l35 = fac.createLink(Id.createLinkId("3->5"), n3, n5);
        net.addLink(l35);
        idIntMapper.addDestinationsLinkMapping(3, 5, l35);

        Link l510 = fac.createLink(Id.createLinkId("5->10"), n5, n10);
        net.addLink(l510);
        idIntMapper.addDestinationsLinkMapping(5, 10, l510);


        for (Link l : net.getLinks().values()) {
            l.setFreespeed(1.33);
            l.setCapacity(2 * 1.33);
            l.setNumberOfLanes(2 * 1.33 / 0.71);
            l.setLength(CoordUtils.calcEuclideanDistance(l.getFromNode().getCoord(), l.getToNode().getCoord()));
        }
        Set<String> ext = new HashSet<>();
        ext.add("car");
        ext.add("ext");
        ext.add("2ext");
        lin.setAllowedModes(ext);
        Set<String> ext2 = new HashSet<>();
        ext2.add("car");
        ext2.add("ext2");
        lout.setAllowedModes(ext2);

    }
}

package casesstudies.statenisland;
/****************************************************************************/
// casim, cellular automaton simulation for multi-destination pedestrian
// crowds; see https://github.com/CACrowd/casim
// Copyright (C) 2016 CACrowd and contributors
/****************************************************************************/
//
//   This file is part of casim.
//   casim is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 2 of the License, or
//   (at your option) any later version.
//
/****************************************************************************/

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;

/**
 * Analyzes travel times from waiting area to ferry
 * Created by laemmel on 31/07/16.
 */
public class EventsFileAnalyzer implements LinkEnterEventHandler, LinkLeaveEventHandler {

    private static final Logger log = Logger.getLogger(EventsFileAnalyzer.class);

    private final Variance stGeorge = new Variance();
    private final Variance whitehall = new Variance();

    private final Id<Link> stGeorgeDest = Id.createLinkId("l71");
    private final Id<Link> stGeorgeOrig1 = Id.createLinkId("HN_0_37-->HN_0_26");
    private final Id<Link> stGeorgeOrig2 = Id.createLinkId("HN_0_37-->HN_0_23");
    private final Id<Link> stGeorgeOrig3 = Id.createLinkId("HN_0_37-->HN_0_25");
    private final Id<Link> stGeorgeOrig4 = Id.createLinkId("HN_0_37-->HN_0_30");

//    private final Scenario sc;
//
//    public EventsFileAnalyzer(Scenario sc) {
//        this.sc = sc;
//    }

    private final Map<Id<Vehicle>, Double> depStGeorge = new HashMap<>();
    private int stGeorgeCnt = 0;


    public static void main(String[] args) {
        String dir = "/Users/laemmel/svn/unimb/NYC-DOTS/TRB2016/matsim/resultsFirstScenario/";
        String it = "29";
//        String netFile = dir + "output_networl.xml.gz";
        String eventsFile = dir + it + ".events.xml.gz";

//        Config c = ConfigUtils.createConfig();
//        Scenario sc = ScenarioUtils.createScenario(c);
//        new MatsimNetworkReader(sc.getNetwork()).readFile(netFile);

        EventsFileAnalyzer analyzer = new EventsFileAnalyzer();

        EventsManager em = EventsUtils.createEventsManager();
        em.addHandler(analyzer);

        new MatsimEventsReader(em).readFile(eventsFile);

        log.info("Walking time analysis (from leaving the waiting area to entering the ferry link");
        log.info("St George Terminal: total departures:" + analyzer.stGeorgeCnt + " average(travel time) = " + analyzer.stGeorge.getMean() + "s var(travel time) = " + analyzer.stGeorge.getVar() + "s sd(travel time) = " + Math.sqrt(analyzer.stGeorge.getVar()) + "s");
        

    }

    @Override
    public void handleEvent(LinkEnterEvent event) {
        if (event.getLinkId() == stGeorgeDest) {
            Double time = depStGeorge.get(event.getVehicleId());
//            System.out.println(time);
            double tt = event.getTime() - time;
            stGeorge.addVar(tt);
            stGeorgeCnt++;
        }



    }

    @Override
    public void reset(int iteration) {

    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        if (event.getLinkId() == stGeorgeOrig1 || event.getLinkId() == stGeorgeOrig2 || event.getLinkId() == stGeorgeOrig3 || event.getLinkId() == stGeorgeOrig4) {
            Id<Vehicle> pId = event.getVehicleId();
            depStGeorge.put(pId, event.getTime());
        }

    }
}

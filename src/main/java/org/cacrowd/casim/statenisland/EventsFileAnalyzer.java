package org.cacrowd.casim.statenisland;
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
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.vehicles.Vehicle;

import java.util.*;

/**
 * Analyzes travel times from waiting area to ferry
 * Created by laemmel on 31/07/16.
 */
public class EventsFileAnalyzer implements LinkEnterEventHandler, LinkLeaveEventHandler {

    private static final Logger log = Logger.getLogger(EventsFileAnalyzer.class);

    private final Variance stGeorge = new Variance();
    private final Variance whitehall = new Variance();
    private final Variance aStGeorge = new Variance();
    private final Variance aWhitehall = new Variance();

    private final Id<Link> stGeorgeDest = Id.createLinkId("l71");
    private final Id<Link> stGeorgeOrig1 = Id.createLinkId("HN_0_37-->HN_0_26");
    private final Id<Link> stGeorgeOrig2 = Id.createLinkId("HN_0_37-->HN_0_23");
    private final Id<Link> stGeorgeOrig3 = Id.createLinkId("HN_0_37-->HN_0_25");
    private final Id<Link> stGeorgeOrig4 = Id.createLinkId("HN_0_37-->HN_0_30");

    private final Id<Link> stGeorgArr1 = Id.createLinkId("HN_0_11-->HN_0_15");
    private final Id<Link> stGeorgArr2 = Id.createLinkId("HN_0_10-->HN_0_14");

    private final Id<Link> whitehallDest = Id.createLinkId("l70");
    private final Id<Link> whitehallOrig1 = Id.createLinkId("HN_1_43-->HN_1_38");
    private final Id<Link> whitehallOrig2 = Id.createLinkId("HN_1_33-->HN_1_38");
    private final Id<Link> whitehallOrig3 = Id.createLinkId("HN_1_33-->HN_1_34");

    private final Id<Link> whitehallArr1 = Id.createLinkId("HN_1_14-->HN_1_18");
    private final Id<Link> whitehallArr2 = Id.createLinkId("HN_1_15-->HN_1_19");
//private final Id<Link> whitehallArr1 = Id.createLinkId("HN_1_26-->HN_1_31");
//private final Id<Link> whitehallArr2 = Id.createLinkId("HN_1_26-->HN_1_31");


    private final Id<Link> whitehallArrival = Id.createLinkId("HN_1_41-->HN_1_44");
    private final Id<Link> stGeorgeArrival = Id.createLinkId("HN_0_35-->HN_0_38");


//    private final Scenario sc;
//
//    public EventsFileAnalyzer(Scenario sc) {
//        this.sc = sc;
//    }

    private final List<Double> stGeorgeTTs = new ArrayList<>();
    private final List<Double> whitehallTTs = new ArrayList<>();
    private final List<Double> arrStGeorgeTTs = new ArrayList<>();
    private final List<Double> arrWhitehallTTs = new ArrayList<>();

    private final Map<Id<Vehicle>, Double> depStGeorge = new HashMap<>();
    private final Map<Id<Vehicle>, Double> depWhitehall = new HashMap<>();
    private final Map<Id<Vehicle>, Double> arrStGeorge = new HashMap<>();
    private final Map<Id<Vehicle>, Double> arrWhitehall = new HashMap<>();
    private int stGeorgeCnt = 0;
    private double stGeorgeDepTime = -1;
    private double stGeorgeArrTime = -1;
    private int whitehallCnt = 0;
    private double whitehallDepTime = -1;
    private double whitehallArrTime = -1;

    private double maxStGeorgeTT = 0;
    private double maxWhitehallTT = 0;

    private double minStGeorgeTT = Double.POSITIVE_INFINITY;
    private double minWhitehallTT = Double.POSITIVE_INFINITY;

    public static void main(String[] args) {

        EventsFileAnalyzer analyzer = new EventsFileAnalyzer();


//        analyzer.stGeorgeDepTime = 31020;
//        analyzer.whitehallDepTime = 32940;
//        String dir = "/Users/laemmel/svn/unimb/NYC-DOTS/TRB2016/matsim/3600peds/it.29/";


//        analyzer.stGeorgeDepTime = 30900;
//        analyzer.whitehallDepTime = 32820;
        String dir = "/Users/laemmel/svn/unimb/NYC-DOTS/TRB2016/matsim/2600peds/it.29/";


        String it = "29";


//        String dir = "/Users/laemmel/svn/unimb/NYC-DOTS/TRB2016/matsim/resultsStressScenario/it.0/";
//        String it = "0";

//        String netFile = dir + "output_networl.xml.gz";
        String eventsFile = dir + it + ".events.xml.gz";

//        Config c = ConfigUtils.createConfig();
//        Scenario sc = ScenarioUtils.createScenario(c);
//        new MatsimNetworkReader(sc.getNetwork()).readFile(netFile);


        EventsManager em = EventsUtils.createEventsManager();
        em.addHandler(analyzer);

        new MatsimEventsReader(em).readFile(eventsFile);

        Collections.sort(analyzer.stGeorgeTTs);
        Collections.sort(analyzer.whitehallTTs);

        int stGeorge5idx = (int) Math.ceil(0.05 * analyzer.stGeorgeTTs.size());
        int stGeorge75idx = (int) Math.ceil(0.75 * analyzer.stGeorgeTTs.size());
        int stGeorge95idx = (int) Math.ceil(0.95 * analyzer.stGeorgeTTs.size());
        int stGeorge99idx = (int) Math.ceil(0.99 * analyzer.stGeorgeTTs.size());

        double stGeorge5perc = analyzer.stGeorgeTTs.get(stGeorge5idx);
        double stGeorge75perc = analyzer.stGeorgeTTs.get(stGeorge75idx);
        double stGeorge95perc = analyzer.stGeorgeTTs.get(stGeorge95idx);
        double stGeorge99perc = analyzer.stGeorgeTTs.get(stGeorge99idx);

        int whitehall5idx = (int) Math.ceil(0.05 * analyzer.whitehallTTs.size());
        int whitehall75idx = (int) Math.ceil(0.75 * analyzer.whitehallTTs.size());
        int whitehall95idx = (int) Math.ceil(0.95 * analyzer.whitehallTTs.size());
        int whitehall99idx = (int) Math.ceil(0.99 * analyzer.whitehallTTs.size());

        double whitehall5perc = analyzer.whitehallTTs.get(whitehall5idx);
        double whitehall75perc = analyzer.whitehallTTs.get(whitehall75idx);
        double whitehall95perc = analyzer.whitehallTTs.get(whitehall95idx);
        double whitehall99perc = analyzer.whitehallTTs.get(whitehall99idx);


        log.info("Walking time analysis (gates opening at the waiting area till entering the ferry link");
        log.info("St George Terminal embarkment: total departures:" + analyzer.stGeorgeCnt + " min(travel time) = "
                + stGeorge5perc + "s max(travel time) = " + analyzer.maxStGeorgeTT
                + "s average(travel time) = " + analyzer.stGeorge.getMean() + "s var(travel time) = "
                + analyzer.stGeorge.getVar() + "s sd(travel time) = " + Math.sqrt(analyzer.stGeorge.getVar()) + "s"
                + " 75th perc (travel time) = " + stGeorge75perc + " 95th perc (travel time) = " + stGeorge95perc
                + " 99th perc (travel time) = " + stGeorge99perc);

        log.info("Whitehall Terminal embarkment: total departures:" + analyzer.whitehallCnt + " min(travel time) = "
                + whitehall5perc + "s max(travel time) = " + analyzer.maxWhitehallTT
                + "s average(travel time) = " + analyzer.whitehall.getMean() + "s var(travel time) = "
                + analyzer.whitehall.getVar() + "s sd(travel time) = " + Math.sqrt(analyzer.whitehall.getVar()) + "s"
                + " 75th perc (travel time) = " + whitehall75perc + " 95th perc (travel time) = " + whitehall95perc
                + " 99th perc (travel time) = " + whitehall99perc);

        Collections.sort(analyzer.arrStGeorgeTTs);
        Collections.sort(analyzer.arrWhitehallTTs);
        int aStGeorge5idx = (int) Math.ceil(0.05 * analyzer.arrStGeorgeTTs.size());
        int aStGeorge75idx = (int) Math.ceil(0.75 * analyzer.arrStGeorgeTTs.size());
        int aStGeorge95idx = (int) Math.ceil(0.95 * analyzer.arrStGeorgeTTs.size());
        int aStGeorge99idx = (int) Math.ceil(0.05 * analyzer.arrStGeorgeTTs.size());
        double aStGeorge5perc = analyzer.arrStGeorgeTTs.get(aStGeorge5idx);
        double aStGeorge75perc = analyzer.arrStGeorgeTTs.get(aStGeorge75idx);
        double aStGeorge95perc = analyzer.arrStGeorgeTTs.get(aStGeorge95idx);
        double aStGeorge99perc = analyzer.arrStGeorgeTTs.get(aStGeorge99idx);

        int aWhitehall5idx = (int) Math.ceil(0.05 * analyzer.arrWhitehallTTs.size());
        int aWhitehall75idx = (int) Math.ceil(0.75 * analyzer.arrWhitehallTTs.size());
        int aWhitehall95idx = (int) Math.ceil(0.95 * analyzer.arrWhitehallTTs.size());
        int aWhitehall99idx = (int) Math.ceil(0.05 * analyzer.arrWhitehallTTs.size());
        double aWhitehall5perc = analyzer.arrWhitehallTTs.get(aWhitehall5idx);
        double aWhitehall75perc = analyzer.arrWhitehallTTs.get(aWhitehall75idx);
        double aWhitehall95perc = analyzer.arrWhitehallTTs.get(aWhitehall95idx);
        double aWhitehall99perc = analyzer.arrWhitehallTTs.get(aWhitehall99idx);
        log.info("St George Terminal disembarkment: total arrivals:" + analyzer.arrStGeorgeTTs.size()
                + " min(travel time) = " + analyzer.arrStGeorgeTTs.get(0) + "s max(travel time) = "
                + analyzer.arrStGeorgeTTs.get(analyzer.arrStGeorgeTTs.size() - 1) + "s average(travel time) = "
                + analyzer.aStGeorge.getMean() + "s var(travel time) = " + analyzer.aStGeorge.getVar()
                + " sd(travel time) = " + Math.sqrt(analyzer.aStGeorge.getVar()) + "s 75th perc (travel time) = "
                + aStGeorge75perc + " 95th perc (travel time) = " + aStGeorge95perc
                + " 99th perc (travel time) = " + aStGeorge99perc);

        log.info("Whitehall Terminal disembarkment: total arrivals:" + analyzer.arrWhitehallTTs.size()
                + " min(travel time) = " + analyzer.arrWhitehallTTs.get(0) + "s max(travel time) = "
                + analyzer.arrWhitehallTTs.get(analyzer.arrWhitehallTTs.size() - 1) + "s average(travel time) = "
                + analyzer.aWhitehall.getMean() + "s var(travel time) = " + analyzer.aWhitehall.getVar()
                + " sd(travel time) = " + Math.sqrt(analyzer.aWhitehall.getVar()) + "s 75th perc (travel time) = "
                + aWhitehall75perc + " 95th perc (travel time) = " + aWhitehall95perc
                + " 99th perc (travel time) = " + aWhitehall99perc);

    }

    @Override
    public void handleEvent(LinkEnterEvent event) {

        if (event.getLinkId() == stGeorgArr1 || event.getLinkId() == stGeorgArr2) { //St George arrival
            Id<Vehicle> pId = event.getVehicleId();
            arrStGeorge.put(pId, event.getTime());
            if (stGeorgeArrTime == -1) {
                stGeorgeArrTime = event.getTime();
            }
        }

        if (event.getLinkId() == whitehallArr1 || event.getLinkId() == whitehallArr2) { //St George arrival
            Id<Vehicle> pId = event.getVehicleId();
            arrWhitehall.put(pId, event.getTime());
            if (whitehallArrTime == -1) {
                whitehallArrTime = event.getTime();
            }
        }

        if (event.getLinkId() == stGeorgeDest) {
            Double time = depStGeorge.get(event.getVehicleId());
//            double tt = event.getTime() - time;
            double tt = event.getTime() - stGeorgeDepTime;// - time;
            if (tt > maxStGeorgeTT) {
                maxStGeorgeTT = tt;
            }
            if (tt < minStGeorgeTT) {
                minStGeorgeTT = tt;
            }
            stGeorgeTTs.add(tt);
            stGeorge.addVar(tt);

        }

        if (event.getLinkId() == whitehallDest) {
            Double time = depWhitehall.get(event.getVehicleId());
//            double tt = event.getTime() - time;
            double tt = event.getTime() - whitehallDepTime;// - time;
            if (tt > maxWhitehallTT) {
                maxWhitehallTT = tt;
            }
            if (tt < minWhitehallTT) {
                minWhitehallTT = tt;
            }
            whitehallTTs.add(tt);
            whitehall.addVar(tt);

        }

        if (event.getLinkId() == stGeorgeArrival) {
            Id<Vehicle> pId = event.getVehicleId();
            double time = arrStGeorge.get(pId);
//            System.out.println(event.getTime() - stGeorgeArrTime);
            arrStGeorgeTTs.add(event.getTime() - stGeorgeArrTime);
            aStGeorge.addVar(event.getTime() - stGeorgeArrTime);
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
            if (stGeorgeDepTime == -1) {
                stGeorgeDepTime = event.getTime();
            }
            stGeorgeCnt++;
        }


        if (event.getLinkId() == whitehallArrival) {
            Id<Vehicle> pId = event.getVehicleId();
            double time = arrWhitehall.get(pId);
//            System.out.println(event.getTime()-whitehallArrTime);
            arrWhitehallTTs.add(event.getTime() - whitehallArrTime);
            aWhitehall.addVar(event.getTime() - whitehallArrTime);
        }


        if (event.getLinkId() == whitehallOrig1 || event.getLinkId() == whitehallOrig2 || event.getLinkId() == whitehallOrig3) {
            Id<Vehicle> pId = event.getVehicleId();
            depWhitehall.put(pId, event.getTime());

//            System.out.println(event.getTime());
            if (whitehallDepTime == -1) {
                whitehallDepTime = event.getTime();
            }
            whitehallCnt++;
        }

    }
}

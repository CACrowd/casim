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

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * Analyzes travel times from waiting area to ferry
 * Created by laemmel on 31/07/16.
 */
public class EventsFileAnalyzer implements LinkEnterEventHandler {

    public EventsFileAnalyzer(Scenario sc) {

    }

    public static void main(String[] args) {
        String dir = "/Users/laemmel/svn/unimb/NYC-DOTS/TRB2016/matsim/resultsFirstScenario/";
        String it = "29";
        String netFile = dir + "output_networl.xml.gz";
        String eventsFile = dir + it + ".events.xml.gz";

        Config c = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(c);
        new MatsimNetworkReader(sc.getNetwork()).readFile(netFile);

        EventsFileAnalyzer analyzer = new EventsFileAnalyzer(sc);


    }

    @Override
    public void handleEvent(LinkEnterEvent event) {

    }

    @Override
    public void reset(int iteration) {

    }
}

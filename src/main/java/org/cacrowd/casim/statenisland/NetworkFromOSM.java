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

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.GeotoolsTransformation;

/**
 * Created by laemmel on 05/07/16.
 */
public class NetworkFromOSM {

    public static void main(String[] args) {
        String baseDir = "/Users/laemmel/svn/unimb/TRB2016/";
        String osmFile = baseDir + "gis/nav.osm";
        String networkFile = baseDir + "matsim/network.xml";

        Config conf = ConfigUtils.createConfig();
        Scenario sc = ScenarioUtils.createScenario(conf);

        Network net = sc.getNetwork();

        CoordinateTransformation transform = new GeotoolsTransformation("EPSG:4326", "EPSG:32618");
        CustomizedOsmNetworkReader reader = new CustomizedOsmNetworkReader(net, transform, false);
        reader.setHighwayDefaults(1, "footway", 1, 1, 1, 1);
        reader.setHighwayDefaults(1, "steps", 1, 1, 1, 1);
        reader.setKeepPaths(true);
        reader.parse(osmFile);

        NetworkWriter writer = new NetworkWriter(net);
        writer.write(networkFile);

    }


}

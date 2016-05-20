package proto.scenario;
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

import matsimconnector.scenario.CAEnvironment;
import org.matsim.api.core.v01.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laemmel on 20/05/16.
 */
public class ProtoCAScenario {


    private final Map<Id<CAEnvironment>, CAEnvironment> environments = new HashMap<>();//TODO: get rid of MATSim IDs? [GL May 16]


    public void addCAEnvironment(CAEnvironment env) {
        this.environments.put(env.getId(), env);
    }
}

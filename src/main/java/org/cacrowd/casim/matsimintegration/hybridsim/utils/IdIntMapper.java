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

package org.cacrowd.casim.matsimintegration.hybridsim.utils;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by laemmel on 15/08/16.
 */
public class IdIntMapper {

    private final Map<Pair<Integer, Integer>, Id<Link>> destLinkMapping = new HashMap<>();
    private final Map<Id<Person>, Integer> persIdIntIdMapping = new HashMap<>();
    private final Map<Id<Node>, Integer> nodeIdIntIdMapping = new HashMap<>();

    private int ids = 0;

    public void addDestinationsLinkMapping(int fromDest, int toDest, Link link) {
        Pair<Integer, Integer> pair = new Pair<>(fromDest, toDest);
        nodeIdIntIdMapping.put(link.getFromNode().getId(), fromDest);
        nodeIdIntIdMapping.put(link.getToNode().getId(), toDest);//only relevant for first link in a chain
        destLinkMapping.put(pair, link.getId());
    }

    public Id<Link> getLinkId(int fromDest, int toDest) {
        Pair<Integer, Integer> pair = new Pair<>(fromDest, toDest);
        return destLinkMapping.get(pair);
    }

    public Integer getIntPersId(Id<Person> id) {
        return persIdIntIdMapping.computeIfAbsent(id, k -> (ids++));
    }

    public Integer getIntNodeId(Id<Node> id) {
        return nodeIdIntIdMapping.get(id);
    }
}

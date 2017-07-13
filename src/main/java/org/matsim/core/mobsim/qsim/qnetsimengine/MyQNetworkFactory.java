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

package org.matsim.core.mobsim.qsim.qnetsimengine;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.qsim.interfaces.AgentCounter;
import org.matsim.vis.snapshotwriters.SnapshotLinkWidthCalculator;


public class MyQNetworkFactory extends QNetworkFactory {

    private EventsManager events;
    private Scenario scenario;
    private NetsimEngineContext context;
    private QNetsimEngine.NetsimInternalInterface netsimEngine;

    public MyQNetworkFactory(EventsManager events, Scenario scenario) {
        this.events = events;
        this.scenario = scenario;
    }

    void initializeFactory(AgentCounter agentCounter, MobsimTimer mobsimTimer, QNetsimEngine.NetsimInternalInterface netsimEngine1) {
        this.netsimEngine = netsimEngine1;
        double effectiveCellSize = this.scenario.getNetwork().getEffectiveCellSize();
        SnapshotLinkWidthCalculator linkWidthCalculator = new SnapshotLinkWidthCalculator();
        linkWidthCalculator.setLinkWidthForVis((double) this.scenario.getConfig().qsim().getLinkWidthForVis());
        linkWidthCalculator.setLaneWidth(this.scenario.getNetwork().getEffectiveLaneWidth());
        AbstractAgentSnapshotInfoBuilder agentSnapshotInfoBuilder = QNetsimEngine.createAgentSnapshotInfoBuilder(this.scenario, linkWidthCalculator);
        this.context = new NetsimEngineContext(this.events, effectiveCellSize, agentCounter, agentSnapshotInfoBuilder, this.scenario.getConfig().qsim(), mobsimTimer, linkWidthCalculator);
    }

    QLinkI createNetsimLink(Link link, QNode toQueueNode) {
        QLinkImpl.Builder linkBuilder = new QLinkImpl.Builder(this.context, this.netsimEngine);
        return linkBuilder.build(link, toQueueNode);
    }

    QNode createNetsimNode(Node node) {
        org.matsim.core.mobsim.qsim.qnetsimengine.QNode.Builder builder = new org.matsim.core.mobsim.qsim.qnetsimengine.QNode.Builder(this.netsimEngine, this.context);
        return builder.build(node);
    }
}

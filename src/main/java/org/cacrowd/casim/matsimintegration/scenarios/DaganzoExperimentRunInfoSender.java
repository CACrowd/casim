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

package org.cacrowd.casim.matsimintegration.scenarios;

import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.proto.HybridSimProto;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;

public class DaganzoExperimentRunInfoSender implements IterationStartsListener {


    private final double bottleneckWidth;
    private final String scenario;
    GRPCExternalClient client;

    public DaganzoExperimentRunInfoSender(GRPCExternalClient client, double bottleneckWidth, String scenario) {
        this.client = client;
        this.bottleneckWidth = bottleneckWidth;
        this.scenario = scenario;
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {

        HybridSimProto.Reset.Builder reset = HybridSimProto.Reset.newBuilder();
        reset.setIteration(event.getIteration());
        client.getBlockingStub().reset(reset.build());

        HybridSimProto.RunInfo.Builder ri = HybridSimProto.RunInfo.newBuilder();
        ri.setRunInfo0(scenario);
        ri.setRunInfo1("width = " + bottleneckWidth + "m");
        ri.setRunInfo2("iteration: " + event.getIteration());
        client.getBlockingStub().runInfo(ri.build());

    }
}

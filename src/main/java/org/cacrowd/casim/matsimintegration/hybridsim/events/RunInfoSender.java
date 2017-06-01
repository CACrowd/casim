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

package org.cacrowd.casim.matsimintegration.hybridsim.events;

import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.proto.HybridSimProto;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;

public class RunInfoSender implements IterationStartsListener {


    GRPCExternalClient client;

    public RunInfoSender(GRPCExternalClient client) {
        this.client = client;
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
        HybridSimProto.RunInfo.Builder ri = HybridSimProto.RunInfo.newBuilder();
        ri.setRunInfo1("width = 0.8 m");
        ri.setRunInfo2("iteration: " + event.getIteration());
        client.getBlockingStub().runInfo(ri.build());
    }
}

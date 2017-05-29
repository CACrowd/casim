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

package org.cacrowd.casim.hybridsim.grpc;

import io.grpc.internal.ManagedChannelImpl;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import org.cacrowd.casim.proto.HybridSimulationGrpc;

import java.util.concurrent.TimeUnit;

public class GRPCExternalClient {

    private final ManagedChannelImpl channel;
    private HybridSimulationGrpc.HybridSimulationBlockingStub blockingStub;

    public GRPCExternalClient(String host, int port) {
        this.channel = NettyChannelBuilder.forAddress(host, port).negotiationType(NegotiationType.PLAINTEXT).build();
        this.blockingStub = HybridSimulationGrpc.newBlockingStub(this.channel);
    }

    public void shutdown() {
        try {
            this.channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public HybridSimulationGrpc.HybridSimulationBlockingStub getBlockingStub() {
        return this.blockingStub;
    }
}

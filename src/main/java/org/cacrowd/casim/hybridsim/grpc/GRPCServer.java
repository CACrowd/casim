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

import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.apache.log4j.Logger;
import org.cacrowd.casim.hybridsim.engine.HybridSimImpl;

import java.io.IOException;

public class GRPCServer {

    private static final Logger log = Logger.getLogger(GRPCServer.class);
    @Inject
    HybridSimImpl hybridSim;
    private Server server;
    private int port = 9000;

    private void start() throws IOException {
        hybridSim.setGRPCServer(this);

        /* The port on which the server should run */
        server = ServerBuilder.forPort(port)
                .addService(hybridSim)
                .build()
                .start();
        log.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GRPCServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void run() throws Exception {
        start();

        blockUntilShutdown();
    }

}

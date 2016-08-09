/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.proto.grpc;


import com.google.inject.Inject;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import org.cacrowd.casim.proto.HybridSimProto;
import org.cacrowd.casim.proto.HybridSimulationGrpc;
import org.cacrowd.casim.proto.engine.CAEngine;


/**
 * Created by laemmel on 05/05/16.
 */
public class CAServer {
    private static Logger log = Logger.getLogger(CAServer.class.getName());
    private Server server;
    private int port = 9000;

    private double simTime = -1;

    @Inject
    private CAEngine engine;

    public static void main(String[] args) throws Exception {
        CAServer server = new CAServer
                ();
        server.run();
    }

    private void start() throws Exception {
        server = ServerBuilder.forPort(port)
                .addService(HybridSimulationGrpc.bindService(new HybridSimImpl()))
                .build()
                .start();
        log.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                this.stop();
                System.err.println("*** server shut down");
            }
        });


    }

    private void stop() {
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

    private final class HybridSimImpl implements HybridSimulationGrpc.HybridSimulation {


        @Override
        public void simulatedTimeInerval(HybridSimProto.LeftClosedRightOpenTimeInterval request, StreamObserver<HybridSimProto.Empty> responseObserver) {
            log.debug("simulateTimeInterval called");


            engine.doSimStep(request.getToTimeExcluding());


            HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }

        @Override
        public void transferAgent(HybridSimProto.Agent request, StreamObserver<HybridSimProto.Boolean> responseObserver) {
            log.debug("transferAgent called");

        }

        @Override
        public void receiveTrajectories(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Trajectories> responseObserver) {

        }

        @Override
        public void retrieveAgents(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Agents> responseObserver) {

        }

        @Override
        public void shutdown(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Empty> responseObserver) {

        }

        @Override
        public void initScenario(HybridSimProto.Scenario request, StreamObserver<HybridSimProto.Empty> responseObserver) {
            //TODO generate CA scenario; solve the upside-down issue

            engine.prepareSim(request);


            HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
            //onPrepareSim in (Proto)CAEngine
        }
    }
}

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

package org.cacrowd.casim.hybridsim.engine;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;
import org.cacrowd.casim.hybridsim.grpc.GRPCServer;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.proto.HybridSimProto;
import org.cacrowd.casim.proto.HybridSimulationGrpc;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.visualizer.VisualizerEngine;

public class HybridSimImpl extends HybridSimulationGrpc.HybridSimulationImplBase {

    private static final Logger log = Logger.getLogger(HybridSimImpl.class);
    @Inject
    HybridSimulationEngine engine;

    @Inject
    SimulationObserver simulationObserver;

    @Inject
    Context context;

    private GRPCServer server;

    @Override
    public void initScenario(HybridSimProto.Scenario request, StreamObserver<HybridSimProto.Empty> responseObserver) {

        log.info("Scenario received.");
        engine.loadEnvironment(request);

        HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void simulatedTimeInerval(HybridSimProto.LeftClosedRightOpenTimeInterval request, StreamObserver<HybridSimProto.Empty> responseObserver) {
//        log.info("simulateTimeInterval called: " + request.getToTimeExcluding());


        engine.doSimInterval(request);


        HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void transferAgent(HybridSimProto.Agent request, StreamObserver<HybridSimProto.Boolean> responseObserver) {
//            log.info("transferAgent called");
        boolean success = engine.tryAddAgent(request);
        HybridSimProto.Boolean resp = HybridSimProto.Boolean.newBuilder().setVal(success).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();

    }

    //
    @Override
    public void receiveTrajectories(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Trajectories> responseObserver) {
//            log.info("receiveTrajectories called");
        HybridSimProto.Trajectories resp = engine.receiveTrajectories();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void queryRetrievableAgents(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Agents> responseObserver) {
        HybridSimProto.Agents resp = engine.getRetrievableAgents();

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void confirmRetrievedAgents(HybridSimProto.Agents request, StreamObserver<HybridSimProto.Empty> responseObserver) {
        engine.confirmRetrievedAgents(request);

        HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void shutdown(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Empty> responseObserver) {
        log.info("shutdown called");
        HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
        this.server.stop();
    }

    @Override
    public void runInfo(HybridSimProto.RunInfo request, StreamObserver<HybridSimProto.Empty> responseObserver) {

        if (simulationObserver instanceof VisualizerEngine) {
            ((VisualizerEngine) simulationObserver).setRunInfo0(request.getRunInfo0());
            ((VisualizerEngine) simulationObserver).setRunInfo1(request.getRunInfo1());
            ((VisualizerEngine) simulationObserver).setRunInfo2(request.getRunInfo2());
        }

        HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    public void setGRPCServer(GRPCServer server) {
        this.server = server;
    }

    @Override
    public void reset(HybridSimProto.Reset request, StreamObserver<HybridSimProto.Empty> responseObserver) {
        context.setIteration(request.getIteration());
        simulationObserver.reset(request.getIteration());
        HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}


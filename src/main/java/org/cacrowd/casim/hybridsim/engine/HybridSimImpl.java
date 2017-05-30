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
import org.cacrowd.casim.proto.HybridSimProto;
import org.cacrowd.casim.proto.HybridSimulationGrpc;

public class HybridSimImpl extends HybridSimulationGrpc.HybridSimulationImplBase {

    private static final Logger log = Logger.getLogger(HybridSimImpl.class);
    @Inject
    HybridSimulationEngine engine;

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

    //    @Override
//    public void retrieveAgents(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Agents> responseObserver) {
//        log.info("retrieveAgents called");
//        HybridSimProto.Agents resp = engine.retrieveArrivedAgents();
//        responseObserver.onNext(resp);
//        responseObserver.onCompleted();
//    }
//
//        @Override
//        public void shutdown(HybridSimProto.Empty request, StreamObserver<HybridSimProto.Empty> responseObserver) {
//            log.info("shutdown called");
//            HybridSimProto.Empty resp = HybridSimProto.Empty.getDefaultInstance();
//            responseObserver.onNext(resp);
//            responseObserver.onCompleted();
//        }
//

}


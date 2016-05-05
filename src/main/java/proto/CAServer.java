package proto;
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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.log4j.Logger;

/**
 * Created by laemmel on 05/05/16.
 */
public class CAServer {
	private static Logger log = Logger.getLogger(CAServer.class.getName());
	private Server server;
	private int port = 9000;


	private CAEngine engine;

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


	public static void main(String[] args) throws Exception {
		CAServer server = new CAServer
				();
		server.start();
		server.blockUntilShutdown();
	}

	private final class HybridSimImpl implements HybridSimulationGrpc.HybridSimulation {


		@Override
		public void simulatedTimeInerval(HybridSimProto.LeftClosedRightOpenTimeInterval request, StreamObserver<HybridSimProto.Empty> responseObserver) {
			for (double time = request.getFromTimeIncluding(); time < request.getToTimeExcluding(); time++) {
				engine.doSimStep(time);
			}
		}

		@Override
		public void transferAgent(HybridSimProto.Agent request, StreamObserver<HybridSimProto.Boolean> responseObserver) {

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

			//onPrepareSim in (Proto)CAEngine
		}
	}
}

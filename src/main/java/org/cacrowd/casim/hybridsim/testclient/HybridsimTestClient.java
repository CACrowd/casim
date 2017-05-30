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

package org.cacrowd.casim.hybridsim.testclient;

import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.proto.HybridSimProto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HybridsimTestClient {

    public static void main(String args[]) {


        HybridSimProto.Scenario sc = createScenario();


        GRPCExternalClient client = new GRPCExternalClient("localhost", 9000);
        client.getBlockingStub().initScenario(sc);

        Iterator<HybridSimProto.Agent> it = generateAgents().iterator();


        HybridSimProto.LeftClosedRightOpenTimeInterval.Builder tb = HybridSimProto.LeftClosedRightOpenTimeInterval.newBuilder();


        double incr = .3;
        for (double time = 0; time < 10000.; time += incr) {
            //transfer some agents (e.g. 4 at most)
            for (int i = 0; i < 4 && it.hasNext(); i++) {
                client.getBlockingStub().transferAgent(it.next());
            }

            //simulate one step (might result in several substeps in casim)
            tb.setFromTimeIncluding(time);
            tb.setToTimeExcluding(time + incr);
            client.getBlockingStub().simulatedTimeInerval(tb.build());

            //receive trajectories and do something meaningful with them
            HybridSimProto.Trajectories trajectories = client.getBlockingStub().receiveTrajectories(HybridSimProto.Empty.getDefaultInstance());

            //check whether there are agents who are ready to be retrieved
            HybridSimProto.Agents abouteToLeave = client.getBlockingStub().queryRetrievableAgents(HybridSimProto.Empty.getDefaultInstance());

            //inform casim which agents are accepted for retrieval (e.g. 3 at most)
            List<HybridSimProto.Agent> confirmed = abouteToLeave.getAgentsList().subList(0, Math.min(3, abouteToLeave.getAgentsList().size()));
            client.getBlockingStub().confirmRetrievedAgents(HybridSimProto.Agents.newBuilder().addAllAgents(confirmed).build());
        }

    }

    private static List<HybridSimProto.Agent> generateAgents() {
        HybridSimProto.Agent.Builder ab = HybridSimProto.Agent.newBuilder();
        HybridSimProto.Destination.Builder db = HybridSimProto.Destination.newBuilder();
        HybridSimProto.Coordinate.Builder cb = HybridSimProto.Coordinate.newBuilder();


        List<HybridSimProto.Agent> ret = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            cb.setX(0.9);
            cb.setY(1);
            ab.setEnterLocation(cb.build());
            cb.setX(19);
            ab.setLeaveLocation(cb.build());
            ab.setId(i);
            ab.clearDests();
            db.setId(0);
            ab.addDests(db.build());
            db.setId(1);
            ab.addDests(db.build());
            db.setId(2);
            ab.addDests(db.build());
            db.setId(3);
            ab.addDests(db.build());
            ret.add(ab.build());
        }
        return ret;
    }

    private static HybridSimProto.Scenario createScenario() {

        HybridSimProto.Scenario.Builder sb = HybridSimProto.Scenario.newBuilder();
        HybridSimProto.Edge.Builder eb = HybridSimProto.Edge.newBuilder();
        HybridSimProto.Coordinate.Builder cb = HybridSimProto.Coordinate.newBuilder();

        cb.setX(0);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setX(20);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(0);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setX(20);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(0);
        cb.setY(4);
        eb.setC0(cb.build());
        cb.setX(20);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(0);
        cb.setY(4);
        eb.setC0(cb.build());
        cb.setY(0);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        cb.setX(0.4);
        cb.setY(3.6);
        eb.setC0(cb.build());
        cb.setY(0.4);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(0);
        sb.addEdges(eb.build());

        cb.setX(2.);
        cb.setY(3.6);
        eb.setC0(cb.build());
        cb.setY(0.4);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(1);
        sb.addEdges(eb.build());

        cb.setX(18.);
        cb.setY(3.6);
        eb.setC0(cb.build());
        cb.setY(0.4);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(2);
        sb.addEdges(eb.build());

        cb.setX(19.6);
        cb.setY(3.6);
        eb.setC0(cb.build());
        cb.setY(0.4);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.TRANSITION);
        eb.setId(3);
        sb.addEdges(eb.build());

        cb.setX(20);
        cb.setY(0);
        eb.setC0(cb.build());
        cb.setY(4);
        eb.setC1(cb.build());
        eb.setType(HybridSimProto.Edge.Type.OBSTACLE);
        sb.addEdges(eb.build());

        return sb.build();
    }
}

/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.cacrowd.casim.matsimintegration.hybridsim.simulation;


import org.apache.log4j.Logger;
import org.cacrowd.casim.hybridsim.grpc.GRPCExternalClient;
import org.cacrowd.casim.matsimintegration.hybridsim.utils.IdIntMapper;
import org.cacrowd.casim.proto.HybridSimProto;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.interfaces.Netsim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QLinkInternalIAdapter;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import org.matsim.core.population.routes.LinkNetworkRouteImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import de.dlr.sumo.hybridsim.HybridSimProto;
//import org.matsim.contrib.hybridsim.proto.HybridSimProto;

public class ExternalEngine implements MobsimEngine {//, MATSimInterfaceServiceGrpc.MATSimInterfaceService {

    private static final Logger log = Logger.getLogger(ExternalEngine.class);

    private final Map<Integer, Integer> lastNextDestinations = new HashMap<>();

    //	private final CyclicBarrier simStepBarrier = new CyclicBarrier(2);
    private final Map<Integer, QVehicle> vehicles = new HashMap<>();
    private final Map<Id<Link>, QLinkInternalIAdapter> adapters = new HashMap<>();
    private final EventsManager em;
    private final Netsim sim;
    private final Network net;
    private final Scenario sc;
    private final IdIntMapper mapper;
//	private final CyclicBarrier clientBarrier = new CyclicBarrier(2);

    private GRPCExternalClient client;

    public ExternalEngine(EventsManager eventsManager, Netsim sim, IdIntMapper mapper, GRPCExternalClient client) {
        this.mapper = mapper;
        this.em = eventsManager;
        this.sim = sim;
        this.net = sim.getScenario().getNetwork();
        this.client = client;
        this.sc = sim.getScenario();
    }

    public void registerAdapter(QLinkInternalIAdapter external2qAdapterLink) {
        this.adapters.put(external2qAdapterLink.getLink().getId(),
                external2qAdapterLink);
    }

    public EventsManager getEventsManager() {
        return this.em;
    }


    @Override
    public void doSimStep(double time) {
        double to = time + 1;
        HybridSimProto.LeftClosedRightOpenTimeInterval req = HybridSimProto.LeftClosedRightOpenTimeInterval.newBuilder()
                .setFromTimeIncluding(time)
                .setToTimeExcluding(to).build();
        HybridSimProto.Empty resp = this.client.getBlockingStub().simulatedTimeInerval(req);

        //retrieve trajectories
        HybridSimProto.Empty reqTr = HybridSimProto.Empty.getDefaultInstance();
        HybridSimProto.Trajectories trs = this.client.getBlockingStub().receiveTrajectories(reqTr);
        for (HybridSimProto.Trajectory tr : trs.getTrajectoriesList()) {
            Integer driver = tr.getId();
            QVehicle veh = this.vehicles.get(driver);
            Id<Link> nextLinkId = veh.getDriver().chooseNextLinkId();
            int lastNext = lastNextDestinations.get(driver);
            int currentNext = tr.getCurrentDest().getId();
            Id<Link> currentLinkId = mapper.getLinkId(lastNext, currentNext);

            if (nextLinkId == currentLinkId) {
                this.em.processEvent(new LinkLeaveEvent(time, veh.getId(), veh.getDriver().getCurrentLinkId()));
                veh.getDriver().notifyMoveOverNode(nextLinkId);
                this.em.processEvent(new LinkEnterEvent(time, veh.getId(), nextLinkId));
                veh.setCurrentLink(this.sc.getNetwork().getLinks().get(nextLinkId));

                this.lastNextDestinations.put(driver, currentNext);
            } else if (currentNext != lastNext) {
                this.em.processEvent(new LinkLeaveEvent(time, veh.getId(), veh.getDriver().getCurrentLinkId()));
                veh.getDriver().notifyMoveOverNode(nextLinkId);
                this.em.processEvent(new LinkEnterEvent(time, veh.getId(), nextLinkId));
                veh.setCurrentLink(this.sc.getNetwork().getLinks().get(nextLinkId));

                nextLinkId = veh.getDriver().chooseNextLinkId();
                this.em.processEvent(new LinkLeaveEvent(time, veh.getId(), veh.getDriver().getCurrentLinkId()));
                veh.getDriver().notifyMoveOverNode(nextLinkId);
                this.em.processEvent(new LinkEnterEvent(time, veh.getId(), nextLinkId));
                veh.setCurrentLink(this.sc.getNetwork().getLinks().get(nextLinkId));

                this.lastNextDestinations.put(driver, currentNext);
            }
            //TODO xyvxvy events
        }

        //retrieve agents
        HybridSimProto.Empty reqRtrv = HybridSimProto.Empty.getDefaultInstance();
        HybridSimProto.Agents aboutToLeave = client.getBlockingStub().queryRetrievableAgents(reqRtrv);
        List<HybridSimProto.Agent> confirmed = aboutToLeave.getAgentsList().stream().filter(a -> {
            QVehicle v = this.vehicles.remove(a.getId());
            QLinkInternalIAdapter ql = this.adapters.get(v.getDriver().getCurrentLinkId());
            if (ql.isAcceptingFromUpstream()) {
                this.em.processEvent(new LinkLeaveEvent(time, v.getId(), v.getCurrentLink().getId()));
                v.getDriver().notifyMoveOverNode(v.getDriver().chooseNextLinkId());
                ql.addFromUpstream(v);
                return true;
            }
            vehicles.put(a.getId(), v);
            return false;
        }).collect(Collectors.toList());
        client.getBlockingStub().confirmRetrievedAgents(HybridSimProto.Agents.newBuilder().addAllAgents(confirmed).build());

    }

    //rpc MATSim --> extern

    @Override
    public void onPrepareSim() {

        //nothing to be done

    }

    @Override
    public void afterSim() {
        //nothing to be done

    }

    @Override
    public void setInternalInterface(InternalInterface internalInterface) {
        // TODO Auto-generated method stub

    }

    public boolean hasSpace(Id<Node> id) {
        //TODO: ask external sim if there is space
        return true;
    }

    public void addFromUpstream(QVehicle veh) {

        PersonDriverAgentImpl driver = (PersonDriverAgentImpl) veh.getDriver();

        Integer extDriverId = mapper.getIntPersId(driver.getId());

        this.vehicles.put(extDriverId, veh);
        Id<Link> currentLinkId = driver.getCurrentLinkId();


        Leg leg = (Leg) driver.getCurrentPlanElement();
        List<Id<Link>> linkIds = ((LinkNetworkRouteImpl) leg.getRoute()).getLinkIds();


        boolean extRd = false;

        HybridSimProto.Agent.Builder ab = HybridSimProto.Agent.newBuilder();
        HybridSimProto.Destination.Builder db = HybridSimProto.Destination.newBuilder();
        ab.setId(extDriverId);


        for (Id<Link> linkId : linkIds) {
            Link l = this.net.getLinks().get(linkId);

            if (!extRd && linkId == currentLinkId) {
                ab.setEnterLocation(HybridSimProto.Coordinate.newBuilder().setX(l.getCoord().getX()).setY(l.getCoord().getY()));
                int dest = mapper.getIntNodeId(l.getToNode().getId());
                lastNextDestinations.put(extDriverId, dest);
                db.setId(dest);
                ab.addDests(db.build());
                extRd = true;
            }
            if (extRd) {
                db.setId(mapper.getIntNodeId(l.getToNode().getId()));
                ab.addDests(db.build());
                if (l.getAllowedModes().contains("ext2")) {
                    ab.setLeaveLocation(HybridSimProto.Coordinate.newBuilder().setX(l.getCoord().getX()).setY(l.getCoord().getY()));
                    break;
                }
            }
        }

        this.client.getBlockingStub().transferAgent(ab.build());

    }


}

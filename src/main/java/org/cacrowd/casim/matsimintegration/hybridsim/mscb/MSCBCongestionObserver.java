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

package org.cacrowd.casim.matsimintegration.hybridsim.mscb;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.mobsim.framework.MobsimTimer;
import org.matsim.core.mobsim.framework.events.MobsimBeforeCleanupEvent;
import org.matsim.core.mobsim.framework.listeners.MobsimBeforeCleanupListener;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.vehicles.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laemmel on 15/12/2016.
 */
public class MSCBCongestionObserver implements VehicleEntersTrafficEventHandler, PersonDepartureEventHandler, LinkLeaveEventHandler, LinkEnterEventHandler, MobsimBeforeCleanupListener {

    private static final double FS_TS = .6; //freespeed travel time threshold
    private final Map<Id<Vehicle>, Event> vehEvents = new HashMap<>();
    private final Map<Id<Vehicle>, Id<Person>> vehPers = new HashMap<>();
    private final Map<Id<Link>, LinkInfo> lis = new HashMap<>();
    @Inject
    Scenario sc;
    @Inject
    EventsManager em;
    @Inject
    MSCBTravelDisutility tc;

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        if (linkEnterEvent.getLinkId().toString().contains("el")) {
            return;
        }

        vehEvents.put(linkEnterEvent.getVehicleId(), linkEnterEvent);

    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        if (event.getLinkId().toString().contains("el")) {
            return;
        }

        vehEvents.put(Id.createVehicleId(event.getPersonId()), event);
    }

    @Override
    public void handleEvent(LinkLeaveEvent linkLeaveEvent) {
        Event e = this.vehEvents.remove(linkLeaveEvent.getVehicleId());
        if (e == null) { //nothing to be done here
            return;
        }
        final double travelTime = linkLeaveEvent.getTime() - e.getTime();
        final Link link = this.sc.getNetwork().getLinks().get(linkLeaveEvent.getLinkId());
        final double freespeedTravelTime = link.getLength() / link.getFreespeed();

        boolean congested = false;
        if (travelTime > Math.ceil(freespeedTravelTime) + FS_TS) {
            congested = true;
        }

        LinkInfo li = this.lis.get(linkLeaveEvent.getLinkId());
        if (li == null) {
            li = new LinkInfo();
            this.lis.put(linkLeaveEvent.getLinkId(), li);
        }

        if (congested) {

            li.congested.add(new Tuple<Event, LinkLeaveEvent>(e, linkLeaveEvent));
        } else {
            if (li.congested.size() > 0) {
                double dissolveTime = li.congested.get(li.congested.size() - 1).getSecond().getTime();
                li.congested.parallelStream().forEach(ll -> {
                    double externalCost = dissolveTime - ll.getSecond().getTime();
                    Id<Person> personId = this.vehPers.get(ll.getSecond().getVehicleId());
//                    double oldCost = this.tc.getExternalCost(linkLeaveEvent.getLinkId(), ll.getFirst().getTime());
                    double oldCost = externalCost;
                    PersonMoneyEvent pm = new PersonMoneyEvent(linkLeaveEvent.getTime(), personId, (oldCost / 3600) * this.sc.getConfig().planCalcScore().getModes().get("car").getMarginalUtilityOfTraveling());
                    this.em.processEvent(pm);
                    CongestionEvent ce = new CongestionEvent(linkLeaveEvent.getTime(), ll.getFirst().getTime(), externalCost, linkLeaveEvent.getLinkId());
                    this.em.processEvent(ce);
                });
            }
            li.congested.clear();
        }
    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent vehicleEntersTrafficEvent) {
        vehPers.put(vehicleEntersTrafficEvent.getVehicleId(), vehicleEntersTrafficEvent.getPersonId());
    }

    @Override
    public void reset(int i) {
        this.vehEvents.clear();
        this.lis.clear();
        this.vehPers.clear();
    }

    @Override
    public void notifyMobsimBeforeCleanup(MobsimBeforeCleanupEvent e) {
        MobsimTimer mt = ((QSim) e.getQueueSimulation()).getSimTimer();
        this.lis.entrySet().parallelStream().forEach(entr -> {
            Id<Link> id = entr.getKey();
            if (entr.getValue().congested.size() < 2) {
                return;
            }
            double dissolveTime = entr.getValue().congested.get(entr.getValue().congested.size() - 1).getSecond().getTime();
            entr.getValue().congested.forEach(ll -> {
                double externalCost = dissolveTime - ll.getSecond().getTime();
                Id<Person> personId = this.vehPers.get(ll.getSecond().getVehicleId());

//                double oldCost = this.tc.getExternalCost(id, ll.getFirst().getTime());
                double oldCost = externalCost;
                PersonMoneyEvent pm = new PersonMoneyEvent(mt.getTimeOfDay(), personId, (oldCost / 3600) * this.sc.getConfig().planCalcScore().getModes().get("car").getMarginalUtilityOfTraveling());
                this.em.processEvent(pm);

                CongestionEvent ce = new CongestionEvent(mt.getTimeOfDay(), ll.getFirst().getTime(), externalCost, id);
                this.em.processEvent(ce);
            });
        });
    }

    private static final class LinkInfo {
        List<Tuple<Event, LinkLeaveEvent>> congested = new ArrayList<>();
    }

}

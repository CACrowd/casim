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
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.MatsimServices;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by laemmel on 15/12/2016.
 */
public class MSCBTravelDisutility implements TravelDisutility, AfterMobsimListener, BeforeMobsimListener, CongestionEventHandler {


    private final static double MSA_OFFSET = 2;

    @Inject
    private Scenario sc;

    @Inject
    private EventsManager em;

    @Inject
    private MatsimServices matsimServices;


    private Map<Id<Link>, LinkInfo> lis = new HashMap<>();

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent afterMobsimEvent) {

    }

    @Override
    public void notifyBeforeMobsim(BeforeMobsimEvent beforeMobsimEvent) {

    }


    @Override
    public double getLinkTravelDisutility(Link link, double v, Person person, Vehicle vehicle) {
        return matsimServices.getLinkTravelTimes().getLinkTravelTime(link, v, person, vehicle) + getExternalCost(link.getId(), v);
//        return getExternalCost(link.getId(), v);
    }

    public double getExternalCost(Id<Link> id, double time) {
        LinkInfo li = this.lis.get(id);
        if (li == null) {
            return getLinkMinimumTravelDisutility(this.sc.getNetwork().getLinks().get(id));
        }


        return li.getExternalCost(time);
    }

    @Override
    public double getLinkMinimumTravelDisutility(Link link) {
        return link.getLength() / link.getFreespeed();
    }


    @Override
    public void handleEvent(CongestionEvent congestionEvent) {
        LinkInfo li = this.lis.get(congestionEvent.getLinkId());
        if (li == null) {
            li = new LinkInfo();
//            li.link = this.sc.getNetwork().getLinks().get(congestionEvent.getLinkId());
            this.lis.put(congestionEvent.getLinkId(), li);
//            return 0;
        }
        li.processObservation(congestionEvent.getCongestionLinkEnterTime(), congestionEvent.getCongestionDuration());
    }

    @Override
    public void reset(int iteration) {
        for (LinkInfo li : this.lis.values()) {
            for (CongestionInfo ci : li.congestionInfoTreeMap.values()) {
                ci.updateCongestionCost(iteration);
            }
        }
    }


    private final class LinkInfo {

//        private Link link;

        private Map<Integer, CongestionInfo> congestionInfoTreeMap = new ConcurrentHashMap<>();

        public double getExternalCost(double time) {
            int slot = (int) (time / MSCBTravelDisutility.this.matsimServices.getConfig().travelTimeCalculator().getTraveltimeBinSize());

            CongestionInfo info = this.congestionInfoTreeMap.get(slot);

            if (info == null) {
                return 0;//MSCBTravelDisutility.this.getLinkMinimumTravelDisutility(link);
            }
            return info.getCongestionCost();

        }

        public void processObservation(double time, double congestionDuration) {
            int slot = (int) (time / MSCBTravelDisutility.this.matsimServices.getConfig().travelTimeCalculator().getTraveltimeBinSize());
            CongestionInfo info = this.congestionInfoTreeMap.get(slot);
            if (info == null) {
                info = new CongestionInfo();
                this.congestionInfoTreeMap.put(slot, info);
            }
            info.processOservation(congestionDuration);

        }
    }

    private final class CongestionInfo {

        double cost = 0;

        double congesationTime = 0;
        double cnt = 0;

        public double getCongestionCost() {
            return cost;

        }

        public void updateCongestionCost(int iteration) {
            if (iteration < MSA_OFFSET) {
                this.cost = congesationTime;
            } else {
                double n = iteration - MSA_OFFSET;
                this.cost = (n / (n + 1.)) * cost + (1. / (n + 1.) * congesationTime);

            }
            congesationTime = 0;//MSCBTravelDisutility.this.getLinkMinimumTravelDisutility(link);
            cnt = 0;
        }

        public void processOservation(double v) {
//            if (v > this.congesationTime) {
//                this.congesationTime = v;
//            }
//
            this.congesationTime = (this.cnt / (this.cnt + 1)) * this.congesationTime + (1. / (this.cnt + 1)) * v;
            this.cnt += 1.;
        }

    }
}

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

package org.cacrowd.casim.matsimintegration.hybridsim.simulation;


import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.FlowAnalyzer;
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.QuantityAnalyzer;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.TravelTime;

import java.util.*;

@Singleton
public class MultiScaleManger implements AfterMobsimListener {

    private static final Logger log = Logger.getLogger(MultiScaleManger.class);


    private static final double mxDeviation = 8;
    private final QuantityAnalyzer qa;
    private final FlowAnalyzer fa;

    private List<MultiScaleProvider> multiScaleProviders = new ArrayList<>();

    private boolean runCA = true;

    private Map<Id<Link>, TreeMap<Integer, LinkState>> lookup = new HashMap<>();
    private Set<Id<Link>> exclude = Sets.newHashSet(Id.createLinkId("origin"), Id.createLinkId("in"), Id.createLinkId("destination"), Id.createLinkId("out"));

    @Inject
    public MultiScaleManger(QuantityAnalyzer qa, FlowAnalyzer fa) {
        this.qa = qa;
        this.fa = fa;
    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {

        NetworkUtils.setNetworkChangeEvents(event.getServices().getScenario().getNetwork(), new ArrayList<>());

        if (runCA) {
            updateLookupTable(event);
        } else {
            if (reviseQSim(event)) {
                runCA = true;
            }
        }

        if (event.getIteration() == 99) {
            runCA = true;
        }

//        if (!runCA) {
        createNetworkChangeEvents(event);
//        }

//        multiScaleProviders.forEach(p -> p.setRunCAIteration(false));
//        event.getServices().getVolumes().getVolumesForLink();
        multiScaleProviders.forEach(p -> p.setRunCAIteration(runCA));
    }

    private void createNetworkChangeEvents(AfterMobsimEvent event) {
        double binSize = event.getServices().getConfig().travelTimeCalculator().getTraveltimeBinSize();
        Scenario sc = event.getServices().getScenario();
        List<NetworkChangeEvent> events = new ArrayList<>();


        for (Link l : sc.getNetwork().getLinks().values()) {
            if (exclude.contains(l.getId())) {
                continue;
            }

            TreeMap<Integer, LinkState> lookupTableSpd = lookup.computeIfAbsent(l.getId(), k -> new TreeMap<>());
//            int[] volumes = event.getServices().getVolumes().getVolumesForLink(l.getId());
            int[] volumes = qa.getQuantitySlotsForLink(l.getId());
            if (volumes == null) {
                continue;
            }
            for (int i = 0; i < volumes.length; i++) {
                int volume = volumes[i];
//                if (!lookupTable.containsKey(volume)) {
//                    continue;
//                }
                Map.Entry<Integer, LinkState> cE = lookupTableSpd.ceilingEntry(volume);
                Map.Entry<Integer, LinkState> fE = lookupTableSpd.floorEntry(volume);

                double spd;
                double lanes;
                double cap;
                if (cE == null && fE != null) {
                    spd = fE.getValue().freeSpeed;
                    lanes = fE.getValue().lanes;
                    cap = fE.getValue().flowCap;
                } else if (cE != null && fE == null) {
                    spd = cE.getValue().freeSpeed;
                    lanes = cE.getValue().lanes;
                    cap = cE.getValue().flowCap;
                } else if (cE == null) {
                    continue;
                } else {
                    double spd1 = fE.getValue().freeSpeed;
                    double spd2 = cE.getValue().freeSpeed;
                    double lanes1 = fE.getValue().lanes;
                    double lanes2 = cE.getValue().lanes;
                    double cap1 = fE.getValue().flowCap;
                    double cap2 = cE.getValue().flowCap;
                    double range = cE.getKey() - fE.getKey();
                    if (range == 0) {
                        spd = spd1;
                        lanes = lanes1;
                        cap = cap1;
                    } else {
                        double w1 = 1 - (volume - fE.getKey()) / range;
                        double w2 = 1 - (cE.getKey() - volume) / range;
                        spd = w1 * spd1 + w2 * spd2;
                        lanes = w1 * lanes1 + w2 * lanes2;
                        cap = w1 * cap1 + w2 * cap2;
                    }
                }


                lanes = Math.max(lanes, 1.);
                cap = Math.max(cap, .5);

                if (spd >= .4 / .3) {
                    lanes = l.getNumberOfLanes();
                    cap = l.getCapacity();
                }
//
//                spd = 1.33;
                double time = i * binSize;

                NetworkChangeEvent.ChangeValue changeValueS = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, spd);
                NetworkChangeEvent.ChangeValue changeValueL = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, lanes);
                NetworkChangeEvent.ChangeValue changeValueC = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, cap);

                {
                    NetworkChangeEvent ev = new NetworkChangeEvent(time);
                    ev.setFreespeedChange(changeValueS);
                    ev.setFlowCapacityChange(changeValueC);
                    ev.setLanesChange(changeValueL);
                    ev.addLink(l);
                    events.add(ev);
                }
                {
                    NetworkChangeEvent ev = new NetworkChangeEvent(time + 1);
                    ev.setFreespeedChange(changeValueS);
                    ev.setFlowCapacityChange(changeValueC);
                    ev.setLanesChange(changeValueL);
                    ev.addLink(l);
                    events.add(ev);
                }

            }
        }
        NetworkUtils.setNetworkChangeEvents(sc.getNetwork(), events);
    }

    private boolean reviseQSim(AfterMobsimEvent event) {
        Scenario sc = event.getServices().getScenario();
        for (Link l : sc.getNetwork().getLinks().values()) {
            if (exclude.contains(l.getId())) {
                continue;
            }

            TreeMap<Integer, LinkState> lookupTable = lookup.computeIfAbsent(l.getId(), k -> new TreeMap<>());
//            int[] volumes = event.getServices().getVolumes().getVolumesForLink(l.getId());
            int[] volumes = qa.getQuantitySlotsForLink(l.getId());
            if (volumes == null) {
                continue;
            }
            for (int volume : volumes) {

                if (volume == 0) {
                    continue;
                }
                Map.Entry<Integer, LinkState> cE = lookupTable.ceilingEntry(volume);
                Map.Entry<Integer, LinkState> fE = lookupTable.floorEntry(volume);
                double minDist = Double.POSITIVE_INFINITY;
                if (cE != null) {
                    double dist = cE.getKey() - volume;
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
                if (fE != null) {// && fE.getKey() > 0) {
                    double dist = volume - fE.getKey();
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }

                double deviation = Math.abs(volume - minDist);

                if (minDist > mxDeviation) {
                    log.info("Queue model needs revision, min dist is: " + minDist + " link: " + l.getId());
                    return true;
                }
            }

        }
        return false;
    }

    private void updateLookupTable(AfterMobsimEvent event) {
        double binSize = event.getServices().getConfig().travelTimeCalculator().getTraveltimeBinSize();
        Scenario sc = event.getServices().getScenario();
        TravelTime tt = event.getServices().getLinkTravelTimes();

//        capacityEvents.clear();
        for (Link l : sc.getNetwork().getLinks().values()) {

            if (exclude.contains(l.getId())) {
                continue;
            }

            double obsCapacity = 0;
            TreeMap<Integer, LinkState> lookupTable = lookup.computeIfAbsent(l.getId(), k -> new TreeMap<>());
//            int[] volumes = event.getServices().getVolumes().getVolumesForLink(l.getId());
            int[] volumes = qa.getQuantitySlotsForLink(l.getId());

            if (volumes == null) {
                continue;
            }
            for (int slot = 0; slot < volumes.length; slot++) {
                double time = slot * binSize;
                int volume = volumes[slot];
                double flow = fa.getFlow(time, l.getId());
//                double tmpCapacity = volume / binSize;
//                if (tmpCapacity > obsCapacity) {
//                    obsCapacity = tmpCapacity;
//                }
                double spd = l.getLength() / (tt.getLinkTravelTime(l, time, null, null));
                LinkState ls = new LinkState();
                ls.freeSpeed = spd;
                ls.flowCap = flow;

                double perLane = l.getLength() / sc.getNetwork().getEffectiveCellSize();
                ls.lanes = volume / perLane;

                lookupTable.put(volume, ls);
            }
//            if (l.getCapacity(1) < obsCapacity) {
//                NetworkChangeEvent e = new NetworkChangeEvent(0);
//                NetworkChangeEvent.ChangeValue cv = new NetworkChangeEvent.ChangeValue(NetworkChangeEvent.ChangeType.ABSOLUTE_IN_SI_UNITS, obsCapacity);
//                e.addLink(l);
//                e.setFlowCapacityChange(cv);
//                capacityEvents.put(l.getId(), e);
//            }
        }
        runCA = false;
    }

    public void subscribe(MultiScaleProvider p) {
        this.multiScaleProviders.add(p);
    }

    private static class LinkState {
        double freeSpeed;
        double flowCap;
        double lanes;
    }
}

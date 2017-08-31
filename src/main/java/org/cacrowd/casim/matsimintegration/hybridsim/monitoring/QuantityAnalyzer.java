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

package org.cacrowd.casim.matsimintegration.hybridsim.monitoring;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class QuantityAnalyzer implements LinkEnterEventHandler, LinkLeaveEventHandler, IterationStartsListener, IterationEndsListener, VehicleEntersTrafficEventHandler {


    private static final Logger log = Logger.getLogger(QuantityAnalyzer.class);

    private final double timeBinSize;
    private final double maxTime;
    private final int maxSlotIndex;

    private final Map<Id<Link>, int[]> links = new HashMap<>();

    private int lastSlot = 0;

    public QuantityAnalyzer(double timeBinSize, double maxTime) {
        this.timeBinSize = timeBinSize;
        this.maxTime = maxTime;
        this.maxSlotIndex = (int) ((this.maxTime / this.timeBinSize) + 1);
    }


    @Override
    public void handleEvent(LinkEnterEvent event) {
        int[] qntty = links.computeIfAbsent(event.getLinkId(), k -> new int[maxSlotIndex + 1]);
        int slot = getTimeSlot(event.getTime());
        if (slot > lastSlot) {
            progressTimeSlots(slot);
        }
        qntty[slot]++;

    }

    @Override
    public void handleEvent(VehicleEntersTrafficEvent event) {
        int[] qntty = links.computeIfAbsent(event.getLinkId(), k -> new int[maxSlotIndex + 1]);
        int slot = getTimeSlot(event.getTime());
        if (slot > lastSlot) {
            progressTimeSlots(slot);
        }
        qntty[slot]++;
    }

    private void progressTimeSlots(int slot) {
        links.values().parallelStream().forEach(f -> {
            for (int i = lastSlot + 1; i <= slot; i++) {
                f[i] = f[lastSlot];
            }
        });
        lastSlot = slot;
    }

    private int getTimeSlot(double time) {
        if (time > this.maxTime) {
            return this.maxSlotIndex;
        }
        return (int) (time / this.timeBinSize);
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
        int[] qntty = links.computeIfAbsent(event.getLinkId(), k -> new int[maxSlotIndex + 1]);
        int slot = getTimeSlot(event.getTime());
        if (slot > lastSlot) {
            progressTimeSlots(slot);
        }
        qntty[slot]--;
    }


    public int[] getQuantitySlotsForLink(Id<Link> linkId) {
        return links.get(linkId);
    }

    @Override
    public void reset(int iteration) {

    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {

        links.clear();
        lastSlot = 0;
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        printInfo();
    }

    private void printInfo() {
        log.info("Link quantities: ");
        links.forEach((key, value) -> {
            log.info("Link " + key + " " + Arrays.toString(value));
        });
    }


}

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
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class FlowAnalyzer implements LinkLeaveEventHandler, IterationEndsListener, IterationStartsListener {

    private static final Logger log = Logger.getLogger(FlowAnalyzer.class);

    private final double timeBinSize;
    private final double maxTime;
    private final int maxSlotIndex;

    private final Map<Id<Link>, int[]> links = new HashMap<>();

    public FlowAnalyzer(double timeBinSize, double maxTime) {
        this.timeBinSize = timeBinSize;
        this.maxTime = maxTime;
        this.maxSlotIndex = (int) ((this.maxTime / this.timeBinSize) + 1);
    }


    @Override
    public void handleEvent(LinkLeaveEvent event) {
        int[] qntty = links.computeIfAbsent(event.getLinkId(), k -> new int[maxSlotIndex + 1]);
        int slot = getTimeSlot(event.getTime());
        qntty[slot]--;
    }

    private int getTimeSlot(double time) {
        if (time > this.maxTime) {
            return this.maxSlotIndex;
        }
        return (int) (time / this.timeBinSize);
    }

    @Override
    public void reset(int iteration) {

    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {

        links.clear();
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        printInfo();
    }

    private void printInfo() {
        log.info("Link flows per time bin: ");
        links.forEach((key, value) -> {
            log.info("Link " + key + " " + Arrays.toString(value) + " total: " + IntStream.of(value).sum());
        });
    }

    public double getFlow(double time, Id<Link> id) {
        int[] qntty = links.get(id);
        if (qntty == null) {
            return 0;
        }
        int slot = getTimeSlot(time);
        if (slot >= qntty.length) {
            return 0;
        }
        return -qntty[slot] / timeBinSize;
    }
}

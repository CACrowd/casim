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

import java.util.LinkedHashMap;
import java.util.Map;

import org.cacrowd.casim.matsimintegration.hybridsim.learning.TravelTimeData;
import org.cacrowd.casim.matsimintegration.hybridsim.learning.TravelTimeLookUpTable;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;

public class TravelTimeForLinkAnalyzer implements LinkLeaveEventHandler, LinkEnterEventHandler {

//    private Map<Id<Link>, TravelTimeData> travelTimeForLink = new LinkedHashMap<Id<Link>, TravelTimeData>();
    private Map<Id<Link>, TravelTimeLookUpTable> ttTableForLink = new LinkedHashMap<Id<Link>, TravelTimeLookUpTable>();
    
    @Override
    public void handleEvent(LinkEnterEvent event) {
//        TravelTimeData ttData = travelTimeForLink.computeIfAbsent(event.getLinkId(), k -> new TravelTimeData());
//        ttData.updateTravelTime(event.getVehicleId(), event.getTime());
        TravelTimeLookUpTable ttTable = ttTableForLink.computeIfAbsent(event.getLinkId(), k -> new TravelTimeLookUpTable());
        ttTable.updateTravelTime(event.getVehicleId(), event.getTime());
    }
    
    @Override
    public void handleEvent(LinkLeaveEvent event) {
    	//same code of handleEvent(LinkEnterEvent event)
//        TravelTimeData ttData = travelTimeForLink.computeIfAbsent(event.getLinkId(), k -> new TravelTimeData());
//        ttData.updateTravelTime(event.getVehicleId(), event.getTime());
        TravelTimeLookUpTable ttTable = ttTableForLink.computeIfAbsent(event.getLinkId(), k -> new TravelTimeLookUpTable());
        ttTable.updateTravelTime(event.getVehicleId(), event.getTime());
    }

    @Override
    public void reset(int iteration) {
//        travelTimeForLink.clear();
        ttTableForLink.clear();
    }

    public Map<Id<Link>, TravelTimeData> getTravelTimesForLink() {
    	return null;
//        return new LinkedHashMap<Id<Link>, TravelTimeData>(travelTimeForLink);
    }
    
    public Map<Id<Link>, TravelTimeLookUpTable> getTTTablesForLink() {
        return new LinkedHashMap<Id<Link>, TravelTimeLookUpTable>(ttTableForLink);
    }
}

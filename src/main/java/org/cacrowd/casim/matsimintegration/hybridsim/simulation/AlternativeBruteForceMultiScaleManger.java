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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimintegration.hybridsim.learning.TravelTimeData;
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.TravelTimeForLinkAnalyzer;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AlternativeBruteForceMultiScaleManger implements MultiScaleManger, AfterMobsimListener {

    private static final Logger log = Logger.getLogger(AlternativeBruteForceMultiScaleManger.class);


    private static final double mxDeviation = 8;
    private final TravelTimeForLinkAnalyzer travelTimeForLinkAnalyzer;

    private List<MultiScaleProvider> multiScaleProviders = new ArrayList<>();

    private boolean runCA = true;


    private Set<Id<Link>> incl = Sets.newHashSet(Id.createLinkId("in"), Id.createLinkId("7->8"), Id.createLinkId("8->9"), Id.createLinkId("9->10"), Id.createLinkId("out"));
    private Map<Id<Link>, Params> paramsMap = new HashMap<>();
    private Map<Id<Link>, TravelTimeData> targetTTMap = null;

    private double oldDeviation = Double.POSITIVE_INFINITY;

    @Inject
    public AlternativeBruteForceMultiScaleManger(Scenario sc, TravelTimeForLinkAnalyzer ttForLinkAnalyzer) {

        this.travelTimeForLinkAnalyzer = ttForLinkAnalyzer;

        for (Id<Link> id : incl) {
            Link l = sc.getNetwork().getLinks().get(id);
            Params params = new Params();
            params.flow = l.getFlowCapacityPerSec();
            params.flCoeff = 1;
            params.oldFlCoeff = 1;
            params.freespeed = l.getFreespeed();
            params.fsCoeff = 1;
            params.oldFsCoeff = 1;
            params.lanes = l.getNumberOfLanes();
            params.lCoeff = 1;
            params.oldLCoeff = 1;
            paramsMap.put(id, params);
        }
    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {

        NetworkUtils.setNetworkChangeEvents(event.getServices().getScenario().getNetwork(), new ArrayList<>());
                
        double deviation = 0;
        if (runCA) {
            this.targetTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
            runCA = false;
            deviation = Double.POSITIVE_INFINITY;
        }        
        else{
        	//Error function
	        Map<Id<Link>, TravelTimeData> currentTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
	        //WARNING: 	      this will not work in case different links have been used in the current iteration (i.e. re-routing is activated)
	        //SECOND WARNING: the check of "origin" and "destination" links is temporary, yet logic since they should not require any learning process. 
	        //				  Needs improvement for a general implementation.
	        for (Id<Link> link_id : currentTTMap.keySet()){
	        	if (link_id.toString().equals("destination") || link_id.toString().equals("origin")){
	        		continue;
	        	}
	        	double currentMinTTForLink = currentTTMap.get(link_id).getMinTravelTime();
				double targetMinTTForLink = targetTTMap.get(link_id).getMinTravelTime();
				log.warn("link_id : "+link_id+" - currentMinTT : "+currentMinTTForLink+" - targetMinTT : "+targetMinTTForLink);
				deviation+= Math.abs(currentMinTTForLink - targetMinTTForLink);
				
				double currentMaxTTForLink = currentTTMap.get(link_id).getMaxTravelTime();
				double targetMaxTTForLink = targetTTMap.get(link_id).getAvgMaxTravelTime();
				log.warn("link_id : "+link_id+" - currentMaxTT : "+currentMaxTTForLink+" - targetMaxTT : "+targetMaxTTForLink);
				deviation+= Math.abs(currentMaxTTForLink - targetMaxTTForLink);				
	        }
        }

        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        log.info("Current deviation: " + deviation + "   old deviation:" + oldDeviation);


        if (deviation > oldDeviation) {
            log.info("Testet params rejected!");
            rejectParams();
        } else {
            log.info("Testet params approved!");
            approveParams();
            oldDeviation = deviation;
        }
        
        
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        if (deviation > 1) {
            randomizeParams();
        } else {
//            runCA = true;
        }

        applyParams(event);

//        if (reviseQSim(event)){
//
//        }

//        if (runCA) {
//            updateLookupTable(event);
//        } else {
//            if (reviseQSim(event)) {
//                runCA = true;
//            }
//        }
//
//        if (event.getIteration() == 99) {
//            runCA = true;
//        }
//
////        if (!runCA) {
//        createNetworkChangeEvents(event);
////        }

//        multiScaleProviders.forEach(p -> p.setRunCAIteration(false));
//        event.getServices().getVolumes().getVolumesForLink();
        multiScaleProviders.forEach(p -> p.setRunCAIteration(runCA));
    }

    private void approveParams() {
        for (Params p : this.paramsMap.values()) {
            p.oldFsCoeff = p.fsCoeff;
            p.oldFlCoeff = p.flCoeff;
            p.oldLCoeff = p.lCoeff;
        }
    }

    private void rejectParams() {
        for (Params p : this.paramsMap.values()) {
            p.fsCoeff = p.oldFsCoeff;
            p.flCoeff = p.oldFlCoeff;
            p.lCoeff = p.oldLCoeff;
        }
    }

    private void applyParams(AfterMobsimEvent event) {
        Scenario sc = event.getServices().getScenario();
        for (Id<Link> id : incl) {
            Link l = sc.getNetwork().getLinks().get(id);
            Params params = paramsMap.get(id);
            l.setFreespeed(params.freespeed * params.fsCoeff);
            l.setNumberOfLanes(params.lanes * params.lCoeff);
            l.setCapacity(params.flow * params.flCoeff);
        }
    }

    private void randomizeParams() {
        for (Params p : this.paramsMap.values()) {
            if (p.fsCoeff > 3 || p.fsCoeff < 0.2) {
                p.fsCoeff = 1;
            }
            if (p.flCoeff > 3 || p.flCoeff < 0.2) {
                p.flCoeff = 1;
            }
            if (p.lCoeff > 3 || p.lCoeff < 0.2) {
                p.lCoeff = 1;
            }

//            double rfs = 1 + (MatsimRandom.getRandom().nextDouble() - .5) / 10;
            double rfl = 1 + (MatsimRandom.getRandom().nextDouble() - .5) / 10;
            double rl = 1 + (MatsimRandom.getRandom().nextDouble() - .5) / 10;
//            p.fsCoeff *= rfs;
            p.flCoeff *= rfl;
            p.lCoeff *= rl;
        }
    }


    @Override
    public void subscribe(MultiScaleProvider p) {
        this.multiScaleProviders.add(p);
    }


    private static final class Params {
        double freespeed;
        double fsCoeff;
        double oldFsCoeff;
        double lanes;
        double lCoeff;
        double oldLCoeff;
        double flow;
        double flCoeff;
        double oldFlCoeff;
    }
}

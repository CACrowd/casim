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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimintegration.hybridsim.learning.TravelTimeData;
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.InOutFlowAnalyzer;
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.TravelTimeForLinkAnalyzer;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkWriter;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GreedyCalibratorMultiScaleManager implements MultiScaleManger, AfterMobsimListener {

	private static final Logger log = Logger.getLogger(GreedyCalibratorMultiScaleManager.class);

    private final TravelTimeForLinkAnalyzer travelTimeForLinkAnalyzer;
    private final InOutFlowAnalyzer flowAnalyzer;
    private Scenario sc = null;

    private List<MultiScaleProvider> multiScaleProviders = new ArrayList<>();

    private boolean runCA = true;
    private static final int runCAPeriod = 10;    //number of iterations granted to the GA algorithm before a new observation is performed


    private Set<Id<Link>> incl = Sets.newHashSet(); 
    
//    Sets.newHashSet(Id.createLinkId("in"),Id.createLinkId("in1"), Id.createLinkId("7->8"), Id.createLinkId("8->9")
//            , Id.createLinkId("9->10"), Id.createLinkId("7->4"), Id.createLinkId("4->2"), Id.createLinkId("2->0")
//            , Id.createLinkId("0->1"), Id.createLinkId("1->3"), Id.createLinkId("3->5"), Id.createLinkId("5->10")); //
    
    private Map<Id<Link>, TravelTimeData> targetTTMap = null;
    private Map<Id<Link>, Double> targetOutflows = null;
    private Map<Id<Link>, Integer> targetStorageCaps = null;
    private double paramsStep = .3;
    
    private Map<Id<Link>, Params> paramsMap = new LinkedHashMap<Id<Link>, Params> ();
    private Map<Id<Link>, Double[]> prevErrorsMap = new LinkedHashMap<Id<Link>, Double[]> ();
    private Map<Id<Link>, Double[]> prevSimsMap = new LinkedHashMap<Id<Link>, Double[]> ();

    @Inject
    public GreedyCalibratorMultiScaleManager(Scenario sc, TravelTimeForLinkAnalyzer ttForLinkAnalyzer, InOutFlowAnalyzer flowAnalyzer) {
        this.travelTimeForLinkAnalyzer = ttForLinkAnalyzer;
        this.flowAnalyzer = flowAnalyzer;
        this.sc  = sc;
        for (Id<Link> id : incl) {
            initParams(id);
        }
    }

	private void initParams(Id<Link> id) {
		Link l = sc.getNetwork().getLinks().get(id);
		if (l != null) {
		    Params params = new Params();
		    params.flow = l.getFlowCapacityPerSec();
		    params.flCoeff = 1;
		    params.freespeed = l.getFreespeed();
		    params.fsCoeff = 1;
		    params.lanes = l.getNumberOfLanes();
		    params.lCoeff = 1;
		    paramsMap.put(id, params);
		}
	}

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {

        NetworkUtils.setNetworkChangeEvents(event.getServices().getScenario().getNetwork(), new ArrayList<>());
        String iterOutputPath = event.getServices().getControlerIO().getIterationPath(event.getIteration());
        new NetworkWriter(event.getServices().getScenario().getNetwork()).write(iterOutputPath + "/" + event.getIteration() + ".network.xml");

        if (runCA) {
        	if (targetOutflows == null){
	            targetTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
	            targetOutflows = flowAnalyzer.getLinksMaxOutflow();
	            targetStorageCaps = flowAnalyzer.getLinksStorageCapacity();
	            for (Id<Link> link_id : targetTTMap.keySet()) {
	            	if(!(incl.contains(link_id) || link_id.toString().equals("out") || link_id.toString().equals("destination") || link_id.toString().equals("origin"))){
	            		incl.add(link_id);
	            		initParams(link_id);
	            	}
	            }
        	}
        	else{
        		Map<Id<Link>, TravelTimeData> currentTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
        	    Map<Id<Link>, Double> currentOutflows = flowAnalyzer.getLinksMaxOutflow();
        	    Map<Id<Link>, Integer> currentStorageCaps = flowAnalyzer.getLinksStorageCapacity();
        	    for (Id<Link> link_id : currentTTMap.keySet()) {
        	    	targetTTMap.computeIfAbsent(link_id, k -> currentTTMap.get(k));
        	    	targetTTMap.get(link_id).updateMinTravelTime(currentTTMap.get(link_id).getMinTravelTime());
        	    	targetOutflows.computeIfAbsent(link_id, k -> currentOutflows.get(k));
        	    	targetOutflows.put(link_id,Math.max(targetOutflows.get(link_id), currentOutflows.get(link_id)));
        	    	targetStorageCaps.computeIfAbsent(link_id, k -> currentStorageCaps.get(k));
        	    	targetStorageCaps.put(link_id,Math.max(targetStorageCaps.get(link_id), currentStorageCaps.get(link_id)));
        	    	if(!(incl.contains(link_id) || link_id.toString().equals("out") || link_id.toString().equals("destination") || link_id.toString().equals("origin"))){
	            		incl.add(link_id);
	        	    	initParams(link_id);
	            	}
        	    }
        	    
        	}
        	
        	runCA = false;

        } else {
            
            Map<Id<Link>, TravelTimeData> currentTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
		    Map<Id<Link>, Double> currentOutflows = flowAnalyzer.getLinksMaxOutflow();
		    Map<Id<Link>, Integer> currentStorageCaps = flowAnalyzer.getLinksStorageCapacity();
		    Network net = event.getServices().getScenario().getNetwork();
		    for (Id<Link> link_id : currentOutflows.keySet()) {
		    	if (targetOutflows.get(link_id) == null || paramsMap.get(link_id) == null || link_id.toString().equals("destination") || link_id.toString().equals("origin"))
		    		continue;
		    	Link l = sc.getNetwork().getLinks().get(link_id);
	            Params params = paramsMap.get(link_id);
		    	Double [] prevErrors = prevErrorsMap.get(link_id);
		    	if (prevErrors ==null ){
		    		Double[] initial = {-2.,-2.,-2.};
		    		prevErrorsMap.put(link_id, initial);
		    		prevErrors = prevErrorsMap.get(link_id);
		    	}
		    	Double [] prevSims = prevSimsMap.get(link_id);
		    	if (prevSims == null){
		    		Double[] initial = {-1.,-1.,-1.};
		    		prevSimsMap.put(link_id, initial);
		    		prevSims = prevSimsMap.get(link_id);
		    	}
	            
		    	double length = net.getLinks().get(link_id).getLength();
				double currentFS = length/currentTTMap.get(link_id).getMinTravelTime();
				double error = Math.signum(length/targetTTMap.get(link_id).getMinTravelTime() - currentFS);
				if (error != 0 && (prevErrors[0] == 0 || (prevErrors[0] != 0 && (error != prevErrors[0] || currentFS != prevSims[0])))){
					params.fsCoeff += (error*paramsStep);
			    	params.fsCoeff = cast(params.fsCoeff);
					l.setFreespeed(params.freespeed * params.fsCoeff);
			    	log.warn(link_id+": currentMinSpeed: " +currentFS+" - target: "+length/targetTTMap.get(link_id).getMinTravelTime()+" -------------> error: "+error);
		    	}
		    	prevErrors[0] = error;
		    	prevSims[0] = currentFS;
		    	
		    	double currentOutflow = currentOutflows.get(link_id);
				error = Math.signum(targetOutflows.get(link_id) - currentOutflow);
				if (error != 0 && (prevErrors[1] == 0 || (prevErrors[1] != 0 && (error != prevErrors[1] || currentOutflow != prevSims[1])))){
					params.flCoeff += (error*paramsStep);
			    	params.flCoeff = cast(params.flCoeff);
			    	l.setCapacity(params.flow * params.flCoeff);
			    	log.warn(link_id+": currentOutflow: " +currentOutflow+" - target: "+targetOutflows.get(link_id)+" -------------> error: "+error);
			    	if (link_id.toString().equals("in"))
			    		log.error(""+ prevSims[1]+ " - "+ prevErrors[1]);
				}
				prevErrors[1] = error;
		    	prevSims[1] = currentOutflow;
		    	
		    	double currentSC = currentStorageCaps.get(link_id);
				error = Math.signum(targetStorageCaps.get(link_id) - currentSC);
				if (error != 0 && (prevErrors[2] == 0 || (prevErrors[2] != 0 && (error != prevErrors[2] || currentSC != prevSims[2])))){
					params.lCoeff += (error*paramsStep);
			    	params.lCoeff = cast(params.lCoeff);
			    	l.setNumberOfLanes(params.lanes * params.lCoeff);
			    	log.warn(link_id+": currentStorageCaps: " +currentSC+" - target: "+targetStorageCaps.get(link_id)+" -------------> error: "+error);	    	
				}		    	
				prevErrors[2] = error;
		    	prevSims[2] = currentSC;
		    	
		    }
                        
            
        }

        paramsStep*=.99;
        runCA = (event.getIteration() + 1) % runCAPeriod == 0 || (event.getIteration() < 3);
        multiScaleProviders.forEach(p -> p.setRunCAIteration(runCA));       
    }
    
    private double cast(double param){
    	if (param>5)
    		return 5;
    	if (param<.2)
    		return .2;
    	return param;
    			
    }

//    private void decayLinkTravelTimes() {
//        for (Id<Link> l : incl) {
//            //GL -- not sure with the "< 50" param; but "targetTTMap.get(l) == null" alone does not work
//            if (targetTTMap.get(l) == null || targetTTMap.get(l).getNRTravelers() < 50) {
//            	paramsMap.get(l).fsCoeff *= 1.1;
//            }
//        }
//    }


    @Override
    public void subscribe(MultiScaleProvider p) {
        this.multiScaleProviders.add(p);
    }


    private static final class Params {
        double freespeed;
        double fsCoeff;
        double lanes;
        double lCoeff;
        double flow;
        double flCoeff;
    }

}

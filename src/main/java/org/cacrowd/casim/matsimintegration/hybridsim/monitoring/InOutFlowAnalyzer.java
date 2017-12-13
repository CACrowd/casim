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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;

public class InOutFlowAnalyzer implements LinkEnterEventHandler, LinkLeaveEventHandler, IterationEndsListener, IterationStartsListener {

    private final double timeBinSize = 5;			//seconds
    private Map<Id<Link>,Double> nextTimeInflow = new HashMap<Id<Link>,Double>();;
    private Map<Id<Link>,Double> nextTimeOutflow = new HashMap<Id<Link>,Double>();;
    
    private Map <Id<Link>, ArrayList<String>> flows = new HashMap<Id<Link>,ArrayList<String>>();
    private final Map<Id<Link>, Double> linksInflow = new HashMap<Id<Link>,Double>();
    private final Map<Id<Link>, Double> linksOutflow = new HashMap<Id<Link>,Double>();
    private final Map<Id<Link>, Double> linksMaxInflow = new HashMap<Id<Link>,Double>();
    private final Map<Id<Link>, Double> linksMaxOutflow = new HashMap<Id<Link>,Double>();

    private final Map<Id<Link>, Integer> linksCurrentStorage = new HashMap<Id<Link>,Integer>();
    private final Map<Id<Link>, Integer> linksStorageCapacity = new HashMap<Id<Link>,Integer>();
     
    @Override
    public void handleEvent(LinkEnterEvent event) {
    	Id<Link> linkId = event.getLinkId();
    	linksStorageCapacity.computeIfAbsent(linkId, k -> 1);
    	int cap = linksCurrentStorage.computeIfAbsent(linkId, k -> 0);
    	linksCurrentStorage.put(linkId, cap + 1);
    	if (linksCurrentStorage.get(linkId) > linksStorageCapacity.get(linkId))
    		linksStorageCapacity.put(linkId, linksCurrentStorage.get(linkId));
    	
		nextTimeInflow.computeIfAbsent(linkId, k -> timeBinSize);
    	if (event.getTime()>=nextTimeInflow.get(linkId)){
    		nextTimeInflow.put(linkId, timeBinSize * ((int)(event.getTime()/timeBinSize + 1)));
    		double maxInflow = linksMaxInflow.computeIfAbsent(linkId, k -> 0.);
    		double currentInflow = linksInflow.computeIfAbsent(linkId, k -> 0.);
    		flows.computeIfAbsent(linkId, k->new ArrayList<String>());
    		flows.get(linkId).add(""+ event.getTime() + ", " + currentInflow);
    		if (currentInflow > maxInflow){
    			linksMaxInflow.put(linkId, currentInflow);
    		}
    		linksInflow.put(linkId, 0.);
    	}      
    	double currentInflow = linksInflow.computeIfAbsent(linkId, k -> 0.);
    	linksInflow.put(linkId, currentInflow + 1.);
    }

    @Override
    public void handleEvent(LinkLeaveEvent event) {
    	Id<Link> linkId = event.getLinkId();
    	linksStorageCapacity.computeIfAbsent(linkId, k -> 0);
    	int cap = linksCurrentStorage.computeIfAbsent(linkId, k -> 0);
    	linksCurrentStorage.put(linkId, cap - 1);
    	
    	
		nextTimeOutflow.computeIfAbsent(linkId, k -> timeBinSize);
    	if (event.getTime() >= nextTimeOutflow.get(linkId)){
    		nextTimeOutflow.put(linkId, timeBinSize * ((int)(event.getTime()/timeBinSize + 1)));
    		double maxOutflow = linksMaxOutflow.computeIfAbsent(linkId, k -> 0.);
    		double currentOutflow = linksOutflow.computeIfAbsent(linkId, k -> 0.);
    		if (currentOutflow > maxOutflow){
    			linksMaxOutflow.put(linkId, currentOutflow);
    		}
    		linksOutflow.put(linkId, 0.);
    	}   
    	double currentOutflow = linksOutflow.computeIfAbsent(linkId, k -> 0.);
        linksOutflow.put(linkId, currentOutflow + 1.);
    }

    

    @Override
    public void reset(int iteration) {

    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent event) {
    	nextTimeInflow.clear();
    	nextTimeOutflow.clear();
    	linksInflow.clear();
        linksOutflow.clear();
        linksMaxInflow.clear();
        linksMaxOutflow.clear();
  		linksStorageCapacity.clear();
  		linksCurrentStorage.clear();
    }

    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
//    	try {
//			String iterOutputPath = event.getServices().getControlerIO().getIterationPath(event.getIteration());
//			File outputFile = new File(iterOutputPath + "/maxFlowPerLink.csv");
//			outputFile.createNewFile();
//			FileWriter csvWriter = new FileWriter(outputFile);
//			char SEPARATOR = ',';
//			csvWriter.write("#link_id" + SEPARATOR + "max inflow [1/s]" + SEPARATOR + "max outflow [1/s]" + "\n");
//			for (Id<Link> link_id : linksMaxOutflow.keySet()) {
//		        csvWriter.write("" + link_id + SEPARATOR + linksMaxInflow.get(link_id) + SEPARATOR + linksMaxOutflow.get(link_id) + "\n");                    
//			}
//			csvWriter.close();
//			
//			outputFile = new File(iterOutputPath + "/flowPerLink.csv");
//			outputFile.createNewFile();
//			csvWriter = new FileWriter(outputFile);
//			csvWriter.write("#link_id" + SEPARATOR + "time [s]" + SEPARATOR + "flow [1/s]" + "\n");
//			for (Id<Link> link_id : flows.keySet()) {
//				for (String row : flows.get(link_id)) {
//					csvWriter.write("" + link_id + SEPARATOR + row + "\n");
//				}
//			}
//			csvWriter.close();
//		} catch (IOException e) {
//		    e.printStackTrace();
//		}
    }
    
    public Map <Id<Link>,Double> getLinksMaxOutflow(){
    	return new LinkedHashMap<Id<Link>, Double>(linksMaxOutflow);
    }
    
    public Map <Id<Link>, Integer> getLinksStorageCapacity(){
    	return new LinkedHashMap<Id<Link>, Integer>(linksStorageCapacity);
    }

}

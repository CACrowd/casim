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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimintegration.hybridsim.learning.TravelTimeData;
import org.cacrowd.casim.matsimintegration.hybridsim.learning.TravelTimeLookUpTable;
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.InOutFlowAnalyzer;
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.TravelTimeForLinkAnalyzer;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.NetworkWriter;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GeneticAlgorithmMultiScaleManger implements MultiScaleManger, AfterMobsimListener {

	private static final Logger log = Logger.getLogger(GeneticAlgorithmMultiScaleManger.class);

    private final TravelTimeForLinkAnalyzer travelTimeForLinkAnalyzer;
    private final InOutFlowAnalyzer flowAnalyzer;

    private List<MultiScaleProvider> multiScaleProviders = new ArrayList<>();

    private boolean runCA = true;
    private static final int runCAPeriod = 10;    //number of iterations granted to the GA algorithm before a new observation is performed


    @SuppressWarnings("unchecked")
    private Set<Id<Link>> incl = Sets.newHashSet(Id.createLinkId("in"),Id.createLinkId("in1"), Id.createLinkId("7->8"), Id.createLinkId("8->9")
            , Id.createLinkId("9->10"), Id.createLinkId("7->4"), Id.createLinkId("4->2"), Id.createLinkId("2->0")
            , Id.createLinkId("0->1"), Id.createLinkId("1->3"), Id.createLinkId("3->5"), Id.createLinkId("5->10")); //

    //parameters of the genetic algorithm
    private int gaDynastySize = 8;
    private int gaParentsSize = 2;
    private int gaPopulationSize = gaDynastySize + gaParentsSize;
    private Vector<SolutionGA> gaDynasty = new Vector<SolutionGA>(gaDynastySize);
    private Vector<SolutionGA> gaParents = new Vector<SolutionGA>(gaParentsSize);
    private Vector<SolutionGA> gaPopulation = new Vector<SolutionGA>(gaPopulationSize);
    private int gaInitialMutation = 30;
    private int gaCurrentSolutionIndex = 0;
    
    //initial value of the limit for the free speed parameter
//    private double fsLimit = 10;


    //    private Map<Id<Link>, Params> paramsMap = new HashMap<>();
//    private Map<Id<Link>, TravelTimeLookUpTable> targetTTTables = null;
//    private double alphaDataPersistence = .8;
    private Map<Id<Link>, TravelTimeData> targetTTMap = null;
    private double w_tt = .3;
    private Map<Id<Link>, Double> targetOutflows = null;
    private double w_flow = .6;
    private Map<Id<Link>, Integer> targetStorageCaps = null;
    private double w_cap = .1;

    //this is used to store the output
    private ArrayList<Double> deviations = new ArrayList<Double>();
//    private double oldDeviation = Double.POSITIVE_INFINITY;

    @Inject
    public GeneticAlgorithmMultiScaleManger(Scenario sc, TravelTimeForLinkAnalyzer ttForLinkAnalyzer, InOutFlowAnalyzer flowAnalyzer) {
        this.travelTimeForLinkAnalyzer = ttForLinkAnalyzer;
        this.flowAnalyzer = flowAnalyzer;
        generation(sc);
    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {

        NetworkUtils.setNetworkChangeEvents(event.getServices().getScenario().getNetwork(), new ArrayList<>());
        String iterOutputPath = event.getServices().getControlerIO().getIterationPath(event.getIteration());
        new NetworkWriter(event.getServices().getScenario().getNetwork()).write(iterOutputPath + "/" + event.getIteration() + ".network.xml");

        double deviation = 0;
        if (runCA) {
        	if (targetOutflows == null){
	            targetTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
	            targetOutflows = flowAnalyzer.getLinksMaxOutflow();
	            targetStorageCaps = flowAnalyzer.getLinksStorageCapacity();
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
        	    }
        	    
        	}
        	
        	runCA = false;
        	deviation = Double.POSITIVE_INFINITY;
//            if (targetTTTables == null){
//            	targetTTTables = travelTimeForLinkAnalyzer.getTTTablesForLink();
//            }
//            else {
//            	Map<Id<Link>, TravelTimeLookUpTable> newTTTables = travelTimeForLinkAnalyzer.getTTTablesForLink();
//            	for (Id<Link> link_id : newTTTables.keySet()) {
//            		TravelTimeLookUpTable oldTTTable = targetTTTables.get(link_id);
//            		
//            		//the link has been previously observed, so the new data is
//            		//integrated according to the alpha parameter
//            		if (oldTTTable != null){
//	            		Map<Integer,Double> newTTTable = newTTTables.get(link_id).getTTLookupTable();               
//	    	        	for (int occupation : newTTTable.keySet()) {
//	    	        		oldTTTable.getTTLookupTable().computeIfAbsent(occupation, k -> newTTTable.get(occupation));
//	    	        		oldTTTable.getTTLookupTable().put(occupation, (alphaDataPersistence *oldTTTable.getTTLookupTable().get(occupation) + (1-alphaDataPersistence)*newTTTable.get(occupation)));
//	    	        	}
//	    	        	oldTTTable.maxOccupation = newTTTables.get(link_id).maxOccupation;
//	                }
//            		// First observation on link link_id, so
//            		// direct integration in the targetTTTables map
//            		else {
//            			targetTTTables.put(link_id, newTTTables.get(link_id));
//            		}
//            	}
//            }
//            storeLookupTable(event, iterOutputPath, targetTTTables);
            
        } else {
            //Error function
//            int count_links = 0;
            int deviation_divisor = 0;
//            Map<Id<Link>, TravelTimeLookUpTable> currentTTTables = travelTimeForLinkAnalyzer.getTTTablesForLink();
//            
//		    for (Id<Link> link_id : currentTTTables.keySet()) {
//		        if (link_id.toString().equals("destination") || link_id.toString().equals("origin") || targetTTTables.get(link_id) == null) {
//		            continue;
//		        }
//		        Map <Integer,Double> currentTable = currentTTTables.get(link_id).getTTLookupTable();
//		        Map <Integer,Double> targetTable = targetTTTables.get(link_id).getTTLookupTable();
//		        
////		        double deviation_sum = 0;
////		        for (int occupation : currentTable.keySet()){
////		        	if (targetTable.get(occupation) != null){
////		        		if (targetTable.get(occupation)<1){
////		        			targetTable.put(occupation, 1.0);
////		        		}
////		        		deviation_sum += Math.abs(currentTable.get(occupation) - targetTable.get(occupation))/targetTable.get(occupation);
////		        	}
////		        	else{
////		        		deviation_sum += 0.2;   //penalty for not observed value
////		        	}
////		        }
////		        deviation += deviation_sum/currentTable.keySet().size();
////		        deviation_divisor+=1;
//		        
////		        int currentMaxOccupation = currentTTTables.get(link_id).maxOccupation;
////		        int targetMaxOccupation = targetTTTables.get(link_id).maxOccupation;
////		        if (targetMaxOccupation >= 1){
//////			        double occupancyError = .5*(1 + Math.tanh(Math.abs((double)currentMaxOccupation - targetMaxOccupation)/(10) - 2));
////		        	double occupancyError = Math.abs((double)currentMaxOccupation - targetMaxOccupation)/Math.max(targetMaxOccupation, 10);
////			        deviation += occupancyError;
////			        deviation_divisor+=1;
////			        log.warn(link_id+": current: " +currentMaxOccupation+" - target: "+targetMaxOccupation+" -------------> occupancyError: "+occupancyError);
////		        }
//		        
//		    }
            Map<Id<Link>, TravelTimeData> currentTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
		    Map<Id<Link>, Double> currentOutflows = flowAnalyzer.getLinksMaxOutflow();
		    Map<Id<Link>, Integer> currentStorageCaps = flowAnalyzer.getLinksStorageCapacity();
		    Network net = event.getServices().getScenario().getNetwork();
		    for (Id<Link> link_id : currentOutflows.keySet()) {
		    	if (targetOutflows.get(link_id) == null || link_id.toString().equals("destination") || link_id.toString().equals("origin"))
		    		continue;
//		    	double length = net.getLinks().get(link_id).getLength();
//				double error = w_tt*(Math.abs(length/currentTTMap.get(link_id).getMinTravelTime() - length/targetTTMap.get(link_id).getMinTravelTime())/(length/targetTTMap.get(link_id).getMinTravelTime()));
//		    	deviation += error;
//		    	log.warn(link_id+": currentMinSpeed: " +length/currentTTMap.get(link_id).getMinTravelTime()+" - target: "+length/targetTTMap.get(link_id).getMinTravelTime()+" -------------> error: "+error);
//		    	deviation_divisor+=1;
		    	
		    	double error = w_flow * (Math.abs(currentOutflows.get(link_id) - targetOutflows.get(link_id))/targetOutflows.get(link_id));
				deviation += error;
		    	log.warn(link_id+": currentOutflow: " +currentOutflows.get(link_id)+" - target: "+targetOutflows.get(link_id)+" -------------> error: "+error);
		    	deviation_divisor+=1;
		    	
		    	error = w_cap * (Math.abs(currentStorageCaps.get(link_id) - targetStorageCaps.get(link_id))/(float)targetStorageCaps.get(link_id));
				deviation += error;
		    	log.warn(link_id+": currentStorageCaps: " +currentStorageCaps.get(link_id)+" - target: "+targetStorageCaps.get(link_id)+" -------------> error: "+error);
		    	deviation_divisor+=1;
		    	
		    }
            deviation/=deviation_divisor;
            
            
            //WARNING: 	      this will not work in case different links have been used in the current iteration (i.e. re-routing is activated)
            //SECOND WARNING: the check of "origin" and "destination" links is temporary, yet logic since they should not require any learning process.
            //				  Needs improvement for a general implementation.
//            Map<Id<Link>, TravelTimeData> currentTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
//            for (Id<Link> link_id : currentTTMap.keySet()) {
//                if (link_id.toString().equals("destination") || link_id.toString().equals("origin") || targetTTMap.get(link_id) == null) {
//                    continue;
//                }
//
//                double currentMaxTTForLink = currentTTMap.get(link_id).getMaxTravelTime();
//                double targetMaxTTForLink = targetTTMap.get(link_id).getAvgMaxTravelTime();
//                double currentMinTTForLink = currentTTMap.get(link_id).getMinTravelTime();
//                double targetMinTTForLink = targetTTMap.get(link_id).getMinTravelTime();
//                double currentLastEventTimeForLink = currentTTMap.get(link_id).getLastEventTime();
//                double targetLastEventTimeForLink = targetTTMap.get(link_id).getLastEventTime();
//                if (targetMinTTForLink == 0){
//                	targetMinTTForLink = 1;
//                }
//                if (targetMaxTTForLink == 0){
//                	targetMaxTTForLink = 1;
//                }
//                if (targetMaxTTForLink != 0.) {
//                	
//                    log.warn("link_id : " + link_id + " - currentMinTT : " + currentMinTTForLink + " - targetMinTT : " + targetMinTTForLink);
//                    deviation += Math.abs(currentMinTTForLink - targetMinTTForLink) / targetMinTTForLink;
//                	deviation_divisor+=1;
//                	
//                	log.warn("link_id : " + link_id + " - currentMaxTT : " + currentMaxTTForLink + " - targetMaxTT : " + targetMaxTTForLink);
//                    deviation += Math.abs(currentMaxTTForLink - targetMaxTTForLink) / targetMaxTTForLink;
//                    deviation_divisor+=1;
//                    
//                    log.warn("link_id : " + link_id + " - currentLastET : " + currentLastEventTimeForLink + " - targetLastET : " + targetLastEventTimeForLink);
//                    deviation += Math.abs(currentLastEventTimeForLink - targetLastEventTimeForLink) / targetLastEventTimeForLink;
//                    deviation_divisor+=1;
//                    
//                } else {
//                    log.error("link_id : " + link_id + " has targetMaxTT = 0. Calibration of this link is compromised.");
//                }
//            }
			
            gaPopulation.get(gaCurrentSolutionIndex).deviation = deviation;
            //update list of best performing solutions
            updateParents(gaPopulation.get(gaCurrentSolutionIndex));

            log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            log.info("Current deviation: " + deviation + " - best deviation of population: " + gaParents.get(0).deviation);
            deviations.add(deviation);
            log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

//            if (event.getIteration() % 5==0){
//            	storeLookupTable(event, iterOutputPath, currentTTTables);
//            }
            //update of solution to evaluate and execution of mutation/crossover, according to the case
            if (deviation > .04) {
                gaCurrentSolutionIndex += 1;
                if (gaCurrentSolutionIndex == gaPopulationSize) {
                    crossover();
                }
            } else {
                // runCA = true;
                log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                log.info("Q-MODEL CALIBRATED! - NOW START RE-ROUTING");
                log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                try {
                    File outputFile;
                    outputFile = new File(iterOutputPath + "/deviations.csv");
                    outputFile.createNewFile();
                    FileWriter csvWriter = new FileWriter(outputFile);
                    char SEPERATOR = ',';
                    csvWriter.write("It" + SEPERATOR + "deviation" + SEPERATOR + "acceptance" + "\n");
                    int _it = 0;
                    double min_dev = deviations.get(0);
                    for (double _deviation : this.deviations) {
                        short acceptance = 0;
                        if (_deviation < min_dev) {
                            min_dev = _deviation;
                            acceptance = 1;
                        }
                        csvWriter.write("" + _it + SEPERATOR + _deviation + SEPERATOR + acceptance + "\n");
                        _it += 1;
                    }
                    csvWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        decayLinkTravelTimes();
        runCA = (event.getIteration() + 1) % runCAPeriod == 0 || (event.getIteration() < 3);
        applyParams(event, gaPopulation.get(gaCurrentSolutionIndex));
        multiScaleProviders.forEach(p -> p.setRunCAIteration(runCA));       
    }

	private void storeLookupTable(AfterMobsimEvent event, String iterOutputPath, Map<Id<Link>, TravelTimeLookUpTable> table) {
		try {
			File outputFile;
			for (Id<Link> link_id : table.keySet()) {
		    	Link link = event.getServices().getScenario().getNetwork().getLinks().get(link_id);
		    	if (link_id.toString().contains("->"))
		    		outputFile = new File(iterOutputPath + "/lookupTable"+link.getFromNode().getId()+"-"+link.getToNode().getId()+".csv");
		    	else
		    		outputFile = new File(iterOutputPath + "/lookupTable"+link_id+".csv");
		    	outputFile.createNewFile();
		    	FileWriter csvWriter = new FileWriter(outputFile);
		    	char SEPARATOR = ',';
		    	csvWriter.write("#occupation" + SEPARATOR + "tt [s]" + "\n");
		    	Map<Integer,Double> ttTable = table.get(link_id).getTTLookupTable();
		        for (int occupation : ttTable.keySet()) {
		        	csvWriter.write("" + occupation + SEPARATOR + ttTable.get(occupation) + "\n");                    
		        }
		        csvWriter.close();
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

    private void decayLinkTravelTimes() {

        for (Id<Link> l : incl) {

            //GL -- not sure with the "< 50" param; but "targetTTMap.get(l) == null" alone does not work
            if (targetTTMap.get(l) == null || targetTTMap.get(l).getNRTravelers() < 50) {

                //GL -- not sure about this probably we should only decay
                //gaPopulation.get(gaCurrentSolutionIndex).paramsMap.get(l).fsCoeff
                //What do you think Luca?   ---> Yes otherwise we multiply the parameters of the whole population at every iteration
            	gaPopulation.get(gaCurrentSolutionIndex).paramsMap.get(l).fsCoeff *= 1.1;

//                for (SolutionGA s : gaDynasty) {
//                    Params params = s.paramsMap.get(l);
//                    params.fsCoeff *= 1.1;
//
//                }
//                for (SolutionGA s : gaParents) {
//                    Params params = s.paramsMap.get(l);
//                    params.fsCoeff *= 1.1;
//
//                }
//                for (SolutionGA s : gaPopulation) {
//                    Params params = s.paramsMap.get(l);
//                    params.fsCoeff *= 1.1;
//                }
            }
        }
        
//        if (fsLimit > 2){
//        	fsLimit *= .995;
//        }

    }

    private void applyParams(AfterMobsimEvent event, SolutionGA solution) {
        Scenario sc = event.getServices().getScenario();
        for (Id<Link> id : incl) {
            Link l = sc.getNetwork().getLinks().get(id);
            Params params = solution.paramsMap.get(id);
            l.setFreespeed(params.freespeed * params.fsCoeff);
            l.setNumberOfLanes(params.lanes * params.lCoeff);
            l.setCapacity(params.flow * params.flCoeff);
        }
    }

    private void generation(Scenario sc) {
        for (int i = 0; i < gaPopulationSize; i++) {
            gaPopulation.add(new SolutionGA());
            for (Id<Link> id : incl) {
                Link l = sc.getNetwork().getLinks().get(id);
                if (l != null) {
                    Params params = new Params();
                    params.flow = l.getFlowCapacityPerSec();
                    params.flCoeff = 1;
                    params.freespeed = l.getFreespeed();
                    params.fsCoeff = 1;
                    params.lanes = l.getNumberOfLanes();
                    params.lCoeff = 1;
                    gaPopulation.get(i).paramsMap.put(id, params);
                }
            }
            //initial strong mutation for each solution
            mutation(gaPopulation.get(i), gaInitialMutation);
        }
    }
    
    private void updateParents(SolutionGA currentSolution) {
        if (gaParents.size() < gaParentsSize) {
            gaParents.add(gaParents.size(), currentSolution);
        } else {
            SolutionGA shiftingBestSol = currentSolution;
            for (int i = 0; i < gaParentsSize; i++) {
                if (shiftingBestSol.deviation < gaParents.get(i).deviation) {
                    SolutionGA temp = gaParents.get(i);
                    gaParents.set(i, shiftingBestSol);
                    shiftingBestSol = temp;
                }
            }
        }
    }

    /***
     * @param solution -> the solution to be mutated
     * @param strength -> percentage of mutation (+- strength%)
     */
    private void mutation(SolutionGA solution, double strength) {
        for (Params p : solution.paramsMap.values()) {
//        	if (p.fsCoeff > 2)
//                p.fsCoeff = 2;
//            else if (p.fsCoeff < 0.9)
//                p.fsCoeff = .9;
            if (p.flCoeff > 5)
                p.flCoeff = 5;
            else if (p.flCoeff < 0.2)
                p.flCoeff = .2;
            if (p.lCoeff > 5)
                p.lCoeff = 5;
            else if (p.lCoeff < 0.2)
                p.lCoeff = .2;
//            double rfs = 1 + ((MatsimRandom.getRandom().nextDouble() - 0.5) / 50) * strength;
//            p.fsCoeff *= rfs;
            double rfl = 1 + ((MatsimRandom.getRandom().nextDouble() - 0.5) / 50) * strength;
            p.flCoeff *= rfl;
            double rl = 1 + ((MatsimRandom.getRandom().nextDouble() - 0.5) / 50) * strength;
            p.lCoeff *= rl;
        }
    }

    private void crossover() {
        //generation of the dynasty
        for (int currentSonIndex = 0; currentSonIndex < gaDynastySize; currentSonIndex++) {
            if (currentSonIndex == gaDynasty.size()) {
                gaDynasty.add(new SolutionGA());
            } else {
                gaDynasty.set(currentSonIndex, new SolutionGA());
            }
            //WARNING: here it is assumed that all solutions work on the same set of links, so the first parent is considered as reference
            for (Id<Link> linkId : gaParents.get(0).paramsMap.keySet()) {
                int selectedParent = MatsimRandom.getRandom().nextInt(gaParentsSize);
                Params linkParams = new Params(gaParents.get(selectedParent).paramsMap.get(linkId));
                gaDynasty.get(currentSonIndex).paramsMap.put(linkId, linkParams);
            }
        }

        //population update: parents are kept at each iteration
        for (int i = 0; i < gaParentsSize; i++) {
            //close to the optimal solution
            if (gaParents.get(i).deviation <= .1)
                mutation(gaParents.get(i), 5);
            else
                mutation(gaParents.get(i), 20);
            gaPopulation.set(i, gaParents.get(i));
        }
        for (int i = gaParentsSize; i < gaPopulationSize; i++) {
            mutation(gaDynasty.get(i - gaParentsSize), 5);
            gaPopulation.set(i, gaDynasty.get(i - gaParentsSize));
        }
        gaCurrentSolutionIndex = 0;
    }


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

        Params() {
        }

        Params(Params par) {
            this.freespeed = par.freespeed;
            this.fsCoeff = par.fsCoeff;
            this.lanes = par.lanes;
            this.lCoeff = par.lCoeff;
            this.flow = par.flow;
            this.flCoeff = par.flCoeff;
        }
//        double oldFsCoeff;
//        double oldLCoeff;
//        double oldFlCoeff;
    }

    private static final class SolutionGA {
        Map<Id<Link>, Params> paramsMap = new LinkedHashMap<Id<Link>, Params>();
        double deviation = Double.POSITIVE_INFINITY;
    }
}

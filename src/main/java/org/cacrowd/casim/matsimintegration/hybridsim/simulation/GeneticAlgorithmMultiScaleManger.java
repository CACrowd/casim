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
import org.cacrowd.casim.matsimintegration.hybridsim.monitoring.TravelTimeForLinkAnalyzer;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
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

    private List<MultiScaleProvider> multiScaleProviders = new ArrayList<>();

    private boolean runCA = true;
    //TODO this maybe should be removed in the future...now it is needed to manually start the re-routing once the calibration phase is done 
    private boolean networkCalibration = true;


	private Set<Id<Link>> incl = Sets.newHashSet(); 	
								//Sets.newHashSet(Id.createLinkId("in"), Id.createLinkId("7->8"), Id.createLinkId("8->9"), Id.createLinkId("9->10"), 
								//		Id.createLinkId("7->4"), Id.createLinkId("4->2"), Id.createLinkId("2->0"), Id.createLinkId("0->1"), 
								//		Id.createLinkId("1->3"), Id.createLinkId("3->5"), Id.createLinkId("5->10")); 
    
    
    //parameters of the genetic algorithm
    private int gaDynastySize = 8;
    private int gaParentsSize = 2;
    private int gaPopulationSize = gaDynastySize + gaParentsSize;
    private Vector<SolutionGA> gaDynasty = new Vector<SolutionGA>(gaDynastySize);
    private Vector<SolutionGA> gaParents = new Vector<SolutionGA>(gaParentsSize);
    private Vector<SolutionGA> gaPopulation = new Vector <SolutionGA> (gaPopulationSize);
    private int gaInitialMutation = 30;
    private int gaCurrentSolutionIndex = 0;
    
    
//    private Map<Id<Link>, Params> paramsMap = new HashMap<>();
    private Map<Id<Link>, TravelTimeData> targetTTMap = null;

    //this is used to store the output
    private ArrayList<Double> deviations = new ArrayList<Double>();
//    private double oldDeviation = Double.POSITIVE_INFINITY;


    @Inject
    public GeneticAlgorithmMultiScaleManger(Scenario sc, TravelTimeForLinkAnalyzer ttForLinkAnalyzer) {
        this.travelTimeForLinkAnalyzer = ttForLinkAnalyzer;
    }

    @Override
    public void notifyAfterMobsim(AfterMobsimEvent event) {
    	if (!networkCalibration ){
    		return;
    	}
    	
    	
        NetworkUtils.setNetworkChangeEvents(event.getServices().getScenario().getNetwork(), new ArrayList<>());
        String iterOutputPath = event.getServices().getControlerIO().getIterationPath(event.getIteration());
		new NetworkWriter(event.getServices().getScenario().getNetwork()).write(iterOutputPath+"/"+event.getIteration()+".network.xml");
                
        double deviation = 0;
        if (runCA) {
            this.targetTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
            generation(event.getServices().getScenario());            
            runCA = false;
            deviation = Double.POSITIVE_INFINITY;
        }        
        else{        	
        	//Error function
	        Map<Id<Link>, TravelTimeData> currentTTMap = travelTimeForLinkAnalyzer.getTravelTimesForLink();
	        int count_links = 0;
	        //WARNING: the check of "origin" and "destination" links is temporary, yet logic since they should not require any learning process. 
	        //		   Needs improvement for a general implementation.
	        for (Id<Link> link_id : currentTTMap.keySet()){
	        	if (!targetTTMap.containsKey(link_id) || link_id.toString().equals("destination") || link_id.toString().equals("origin")){
	        		continue;
	        	}
	        	
				double currentMaxTTForLink = currentTTMap.get(link_id).getMaxTravelTime();
				double targetMaxTTForLink = targetTTMap.get(link_id).getAvgMaxTravelTime();
				if (targetMaxTTForLink != 0.){
					count_links += 1;
					log.warn("link_id : "+link_id+" - currentMaxTT : "+currentMaxTTForLink+" - targetMaxTT : "+targetMaxTTForLink);
					deviation+= Math.abs(currentMaxTTForLink - targetMaxTTForLink)/targetMaxTTForLink;	
					
					double currentLastEventTimeForLink = currentTTMap.get(link_id).getLastEventTime();
					double targetLastEventTimeForLink = targetTTMap.get(link_id).getLastEventTime();
					log.warn("link_id : "+link_id+" - currentLastET : "+currentLastEventTimeForLink+" - targetLastET : "+targetLastEventTimeForLink);
					deviation+= Math.abs(currentLastEventTimeForLink - targetLastEventTimeForLink)/targetLastEventTimeForLink;
					
					//energy minimization
					deviation += Math.abs(1-gaPopulation.get(gaCurrentSolutionIndex).paramsMap.get(link_id).fsCoeff)/3.;
					deviation += Math.abs(1-gaPopulation.get(gaCurrentSolutionIndex).paramsMap.get(link_id).flCoeff)/3.;
					deviation += Math.abs(1-gaPopulation.get(gaCurrentSolutionIndex).paramsMap.get(link_id).lCoeff)/3.;
					
				}
				else{
					log.error("link_id : "+link_id+" has targetMaxTT = 0. Calibration of this link is compromised.");
				}
	        }
	        //This is to normalize the deviation value in [0,1] (*2 is due to the double sum above)-> actually it can be higher than 1 since the deviation for each link can be higher than 100%.
	        //After a sufficient number of iterations the value should be lower than 1 though... LC
	        deviation/=(count_links*3);
	        gaPopulation.get(gaCurrentSolutionIndex).deviation = deviation;
	        //update list of best performing solutions
	        updateParents(gaPopulation.get(gaCurrentSolutionIndex));
	        
	        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	        log.info("Current deviation: " + deviation + " - best deviation of population: " + gaParents.get(0).deviation);
	        deviations.add(deviation);       
	        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	        
	        
	        //update of solution to evaluate and execution of mutation/crossover, according to the case
	    	if (deviation > .05){
		        gaCurrentSolutionIndex+=1;	        
		    	if (gaCurrentSolutionIndex == gaPopulationSize){
		    		crossover();
		    	}
	    	}else{
	        	// runCA = true;
	        	log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	        	log.info("Q-MODEL CALIBRATED! - NOW START RE-ROUTING");
	        	log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
	        	try {
	        		File outputFile;
	        		outputFile = new File(iterOutputPath+"/deviations.csv");
	        		outputFile.createNewFile();
	        		FileWriter csvWriter = new FileWriter(outputFile);
	        		char SEPERATOR = ',';
	        		csvWriter.write("It"+SEPERATOR+"deviation"+SEPERATOR+"acceptance"+"\n");
	        		int _it = 0;
	        		double min_dev = deviations.get(0);
	        		for(double _deviation : this.deviations){
	        			short acceptance = 0;
	        			if (_deviation<min_dev){
	        				min_dev = _deviation;
	        				acceptance = 1;
	        			}
	        			csvWriter.write(""+_it+SEPERATOR+_deviation+SEPERATOR+acceptance+"\n");
	        			_it+=1;
	        		}
	        		csvWriter.close();
	        	} catch (IOException e) {
	        		e.printStackTrace();
	        	}
	        }
        }

    	applyParams(event,gaPopulation.get(gaCurrentSolutionIndex));        
        multiScaleProviders.forEach(p -> p.setRunCAIteration(runCA));
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
    
    private void generation(Scenario sc){
    	//init of the set of links to be considered for the calibration
    	for (Id<Link> id : targetTTMap.keySet()){
    		//border links are not considered
    		if(id.toString().equals("destination") || id.toString().equals("origin") || id.toString().equals("out")) {
    			continue;
    		}
    		
    		if (!incl.contains(id)){
    			incl.add(id);
    		}else{
    			//TODO ---> what to do in this case must be clarified: if the set already contains this link
    			// 			it means that the link has been already calibrated in the previous learning process. 
    			//			A possible approach is to keep the link in the set only if the new maximum TT is higher 
    			//			than the target for which it has been previously calibrated.
    			//
    			//			For the simple scenario currently tested, it can just be removed.
    			incl.remove(id);
    		}
    	}
    	
    	for (int i=0;i<gaPopulationSize;i++){
    		gaPopulation.add(new SolutionGA());
    		for (Id<Link> id : incl) {
    			Link l = sc.getNetwork().getLinks().get(id);
    			if (l!= null){
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
		if(gaParents.size()<gaParentsSize){
			gaParents.add(gaParents.size(), currentSolution);
		}
		else {
		    SolutionGA shiftingBestSol = currentSolution;
		    for (int i=0; i<gaParentsSize; i++){
		    	if (shiftingBestSol.deviation < gaParents.get(i).deviation){
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
        	if (p.fsCoeff > 5)
            	p.fsCoeff = 5;
            else if (p.fsCoeff < 0.2) 
                p.fsCoeff = .2; 
        	if (p.flCoeff > 5)
            	p.flCoeff = 5;
            else if (p.flCoeff < 0.2) 
                p.flCoeff = .2;            
            if (p.lCoeff > 5)
            	p.lCoeff = 5;
            else if (p.lCoeff < 0.2) 
                p.lCoeff = .2;
            double rfs = 1 + ((MatsimRandom.getRandom().nextDouble() - 0.5) / 50)*strength;
            p.fsCoeff *= rfs;
            double rfl = 1 + ((MatsimRandom.getRandom().nextDouble() - 0.5) / 50)*strength;
            p.flCoeff *= rfl;
            double rl = 1 + ((MatsimRandom.getRandom().nextDouble() - 0.5) / 50)*strength;
            p.lCoeff *= rl;
        }
    }
    
    private void crossover (){
    	//generation of the dynasty
    	for (int currentSonIndex=0; currentSonIndex<gaDynastySize;currentSonIndex++){
    		if (currentSonIndex==gaDynasty.size()){
    			gaDynasty.add(new SolutionGA());
    		}else{
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
    	for (int i=0; i<gaParentsSize; i++){
    		//close to the optimal solution
    		if (gaParents.get(i).deviation<=.1)
    			mutation(gaParents.get(i),5);
    		else
    			mutation(gaParents.get(i),20);
    		gaPopulation.set(i, gaParents.get(i));
    	}
    	for (int i=gaParentsSize; i<gaPopulationSize; i++){
    		mutation(gaDynasty.get(i-gaParentsSize),5);
    		gaPopulation.set(i, gaDynasty.get(i-gaParentsSize));
    	}
		gaCurrentSolutionIndex = 0;
    }


    @Override
    public void subscribe(MultiScaleProvider p) {
    	p.setRunCAIteration(runCA);
        this.multiScaleProviders.add(p);
    }


    private static final class Params{
    	Params(){
    	}
    	
    	Params(Params par){
    		this.freespeed = par.freespeed;
    		this.fsCoeff = par.fsCoeff;
    		this.lanes = par.lanes;
    		this.lCoeff = par.lCoeff;
    		this.flow = par.flow;
    		this.flCoeff = par.flCoeff;
    	}
    	
        double freespeed;
        double fsCoeff;
        double lanes;
        double lCoeff;
        double flow;
        double flCoeff;
//        double oldFsCoeff;
//        double oldLCoeff;
//        double oldFlCoeff;
    }
    
    private static final class SolutionGA {
    	Map<Id<Link>, Params> paramsMap = new LinkedHashMap<Id<Link>, Params>();
    	double deviation = Double.POSITIVE_INFINITY;
    }
}

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

package org.cacrowd.casim.matsimintegration.hybridsim.learning;

import java.util.LinkedHashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.vehicles.Vehicle;

public class TravelTimeLookUpTable {
	
	public int maxOccupation = 0;
	private static final short occupationDiscretization = 3;
	private int currentOccupation = 0;
	private boolean avgComputed = false;
	private double lastEventTime;
	private Map<Integer, Integer> ttLookupNPoints = new LinkedHashMap<Integer, Integer>();
	private Map<Integer, Double> ttLookup = new LinkedHashMap<Integer, Double>();
	private Map<Id<Vehicle>, OccupationToTT> ttAnalysis = new LinkedHashMap<Id<Vehicle>,OccupationToTT>();
	
	
	public void updateTravelTime(Id<Vehicle> pedId, double time){
		OccupationToTT tableRecord = ttAnalysis.get(pedId);
		//this means that the agent has just entered the link, or it has left the origin link
		if (tableRecord == null){
			ttAnalysis.put(pedId,new OccupationToTT((int)Math.round(currentOccupation/(float)occupationDiscretization), time));
			currentOccupation+=1;
			if (currentOccupation>maxOccupation){
				maxOccupation = currentOccupation;
			}
		}
		//in this case the travel time can be calculated
		else{
			tableRecord.tt = time - tableRecord.tt;
			lastEventTime = time;
			if (ttLookupNPoints.get(tableRecord.occupation) == null){
				ttLookupNPoints.put(tableRecord.occupation,1);
			}else{
				ttLookupNPoints.put(tableRecord.occupation, ttLookupNPoints.get(tableRecord.occupation)+1);
			}
			ttLookup.put(tableRecord.occupation, ttLookup.computeIfAbsent(tableRecord.occupation, k -> new Double(0)) + tableRecord.tt);
			
			currentOccupation-=1;
		}
	}
	
	public double getLastEventTime(){
		return lastEventTime;
	}
	
	/**
	 * LC
	 * @return average of the highest 5% travel times of the link. It overwrites maxTravelTime.
	 */
	public Map<Integer,Double> getTTLookupTable(){
		if (!avgComputed){
			for(int occupation : ttLookup.keySet()){
				ttLookup.put(occupation, ttLookup.get(occupation)/ttLookupNPoints.get(occupation));
			}
			avgComputed = true;
		}				
		return ttLookup;
	}

	public int getNRTravelers() {
		return ttAnalysis.size();
	}
	
	private class OccupationToTT{
		int occupation;
		double tt;
		
		OccupationToTT(int occupation, double tt){
			this.occupation = occupation;
			this.tt = tt;
		}
	}
}

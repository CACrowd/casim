/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.pedca.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.cacrowd.casim.matsimconnector.events.CAAgentChangeLinkEvent;
import org.cacrowd.casim.matsimconnector.events.CAAgentConstructEvent;
import org.cacrowd.casim.matsimconnector.events.CAAgentEnterEnvironmentEvent;
import org.cacrowd.casim.matsimconnector.events.CAAgentExitEvent;
import org.cacrowd.casim.matsimconnector.events.CAAgentLeaveEnvironmentEvent;
import org.cacrowd.casim.matsimconnector.events.CAAgentMoveEvent;
import org.cacrowd.casim.matsimconnector.events.CAAgentMoveToOrigin;
import org.cacrowd.casim.matsimconnector.events.CAEngineStepPerformedEvent;
import org.cacrowd.casim.matsimconnector.events.CAEventHandler;
import org.cacrowd.casim.matsimconnector.utility.IdUtility;
import org.matsim.api.core.v01.Id;

public class TravelTimeAnalyzer implements CAEventHandler{
	
	private int iteration;
	private File csvFile;
//	private File csvFileGlbAvg;
	private HashMap<String, TravelInformation> data = new HashMap<String, TravelInformation> ();
	
	public TravelTimeAnalyzer(String pathName){
		try {
			File path = new File(pathName);
			if(!path.exists())
				new File(pathName).mkdir();			
			
			csvFile = new File(pathName+"/ttData.csv");
			if(csvFile.exists())
				csvFile.delete();			
			csvFile.createNewFile();
			FileWriter csvWriter;
			csvWriter = new FileWriter(csvFile);
			csvWriter.write("#iteration, pedId, IdEnv, StartTime[s], EndTime[s], TT[s]\n");
			csvWriter.close();
			
//			csvFileGlbAvg = new File(pathName+"/ttData_glbAvg.csv");
//			if(csvFileGlbAvg.exists())
//				csvFileGlbAvg.delete();			
//			csvFileGlbAvg.createNewFile();
//			csvWriter = new FileWriter(csvFileGlbAvg);
//			csvWriter.write("#Time[s],TT[1/s]\n");
//			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void reset(int iteration) {
		this.iteration = iteration;
	}

	@Override
	public void handleEvent(CAAgentConstructEvent event) {
	}

	@Override
	public void handleEvent(CAAgentMoveEvent event) {
	}

	@Override
	public void handleEvent(CAAgentExitEvent event) {
	}


	@Override
	public void handleEvent(CAAgentMoveToOrigin event) {
		
	}

	@Override
	public void handleEvent(CAAgentEnterEnvironmentEvent event) {
		data.put(event.getPedestrian().getId().toString(), new TravelInformation(event.getTime()));	
	}	

	@Override
	public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
		Id<Pedestrian> pedId = event.getPedestrian().getId();
		int envId = IdUtility.getEnvironmentId(pedId);
		TravelInformation tt = data.get(pedId.toString());
		tt.endTime = event.getRealTime();
		tt.travelTime = tt.endTime - tt.startTime;
		try {
			FileWriter csvWriter = new FileWriter(csvFile, true);
			csvWriter.write(iteration+","+pedId+","+envId+","+tt.startTime+","+tt.endTime+","+tt.travelTime+"\n");
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}


	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
	}


	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		
	}
	
	private class TravelInformation {	
		double startTime;
		double endTime;
		double travelTime;
		
		TravelInformation(double startTime){
			this.startTime = startTime;
		}
	}
}

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

import org.cacrowd.casim.matsimconnector.events.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FundamentalDiagramWriter implements CAEventHandler{
	
	private final double density;
	private final int populationSize;
	private File csvFile;
	private int pedestrianInside;
	private ArrayList<Double> travelTimes;
	
	public FundamentalDiagramWriter(double density, int populationSize, String outputFileName){
		this.density = density;
		this.populationSize = populationSize;
		this.pedestrianInside = 0;
		this.travelTimes = null;
		try {
			 csvFile = new File(outputFileName);
			 FileWriter csvWriter;
			 if(!csvFile.exists()){
			    csvFile.createNewFile();
			    csvWriter = new FileWriter(csvFile);
			    csvWriter.write("#Density[m^-2],TravelTime[sec]\n");
			    
			}else
				csvWriter = new FileWriter(csvFile,true);
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public FundamentalDiagramWriter(double density, int populationSize, ArrayList<Double> travelTimes){
		this.density = density; 
		this.populationSize = populationSize;
		this.travelTimes = travelTimes; 
	}
	
	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub
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
		if(pedestrianInside == populationSize)
			if (csvFile != null){
				try {
					FileWriter csvWriter = new FileWriter(csvFile,true);
					csvWriter.write(this.density+","+event.getTravelTime()+"\n");
					csvWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				travelTimes.add(event.getTravelTime());
			}
		else{
//			event.getPedestrian().lastTimeCheckAtExit=null;
		}
	}

	@Override
	public void handleEvent(CAAgentEnterEnvironmentEvent event) {
		this.pedestrianInside+=1;
	}	

	@Override
	public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
		this.pedestrianInside-=1;
		
	}


	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
	}


	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		// TODO Auto-generated method stub
		
	}

}

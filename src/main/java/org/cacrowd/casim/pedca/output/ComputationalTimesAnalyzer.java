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

public class ComputationalTimesAnalyzer implements CAEventHandler {
	
	private File csvFile;
	
	public ComputationalTimesAnalyzer(String outputFileName){		
		try {
			 csvFile = new File(outputFileName);
			 FileWriter csvWriter;
			 //if(!csvFile.exists()){
			    csvFile.createNewFile();
			    csvWriter = new FileWriter(csvFile);
			    csvWriter.write("#AGENTS,TIME_FOR_STEP_COMPUTATION");
			    
			//}else
			//	csvWriter = new FileWriter(csvFile,true);
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		try{
			FileWriter csvWriter = new FileWriter(csvFile,true);
		    csvWriter.write(event.getPopulationSize()+","+event.getStepCompTime()+"\n");
		    csvWriter.close();
		}catch (IOException e){
			e.printStackTrace();
		}
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
	}	

	@Override
	public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
	}


	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
	}

}

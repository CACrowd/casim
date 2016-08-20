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
import java.util.Vector;

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

public class CANodeFlowAnalyzer implements CAEventHandler{
	
	private int lastSec;
	private HashMap<String, Integer> dataMap = new HashMap<String, Integer> ();
	private String outputPath;
	private static Vector<String> nodeIds = new Vector<String>();
	static{
		nodeIds.add("HN_1_24"); //slip rampa sx
		nodeIds.add("HN_1_25"); //slip rampa dx
		nodeIds.add("HN_0_14"); //slip rampa sx
		nodeIds.add("HN_0_15"); //slip rampa dx

		nodeIds.add("HN_1_32"); //porta centrale
		nodeIds.add("HN_1_38"); //porta sx
		nodeIds.add("HN_0_23"); //porta centrale
		nodeIds.add("HN_0_26"); //porta dx

		nodeIds.add("HN_1_44"); //flusso in uscita corridoio sx
		nodeIds.add("HN_0_38"); //flusso in uscita corridoio dx
	}
	
	public CANodeFlowAnalyzer(String pathName){
		try {
			outputPath = pathName+"/flowData";
			File path = new File(outputPath);
			if(!path.exists())
				new File(outputPath).mkdir();			
			for (String nodeId : nodeIds){
				String fileName = "/"+nodeId+".csv";
				File csvFile = new File(outputPath+fileName);
				if(csvFile.exists())
					csvFile.delete();			
				csvFile.createNewFile();
				FileWriter csvWriter;
				csvWriter = new FileWriter(csvFile);
				csvWriter.write("#NodeId, Time[s], Flow[1/s]\n");
				csvWriter.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
	}

	@Override
	public void handleEvent(CAAgentEnterEnvironmentEvent event) {
	}	

	@Override
	public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
		
	}


	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
		
		String nodeId = IdUtility.linkIdToDestinationNodeId(event.getFromLinkId());
		if(nodeIds.contains(nodeId)){
			if(!dataMap.containsKey(nodeId))
				dataMap.put(nodeId, 0);
			int nodeFlow = dataMap.get(nodeId);
			dataMap.put(nodeId, nodeFlow+1);
			//totalFlowPerTimeWindow+=1;
		}
	}


	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		int eventSec = (int)event.getTime();
		if (eventSec>lastSec){
			try {
				for (String nodeId : dataMap.keySet()){
					String fileName = "/"+nodeId+".csv";
					File csvFile = new File(outputPath+fileName);
					FileWriter csvWriter = new FileWriter(csvFile,true);
					int instantNodeFlow = dataMap.get(nodeId);
					if (instantNodeFlow > 0){
						csvWriter.write('"'+nodeId+'"'+","+eventSec+","+instantNodeFlow+"\n");
						dataMap.put(nodeId, 0);
					}
					csvWriter.close();					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				lastSec = eventSec;
			}
			
		}
	}
}

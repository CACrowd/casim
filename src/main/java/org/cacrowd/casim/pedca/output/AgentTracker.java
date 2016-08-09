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

import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.cacrowd.casim.matsimconnector.events.*;
import org.cacrowd.casim.matsimconnector.utility.MathUtility;
import org.matsim.api.core.v01.Id;
import org.matsim.core.controler.events.AfterMobsimEvent;
import org.matsim.core.controler.listener.AfterMobsimListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentTracker implements CAEventHandler, AfterMobsimListener {
	public static final char SEPERATOR = '\t';
	private static final double TIME_STEP_SIZE = 0.3;
	private final double maxX;
	private final double maxY;
	private final Map<Id, List<String>> trajectories = new HashMap<>();
	private FileWriter csvWriter;

	public AgentTracker(String outputFileName, int rows, int columns){
		this.maxX = MathUtility.convertGridCoordinate(columns);
		this.maxY = MathUtility.convertGridCoordinate(rows);
		try {
			File outputFile;
			outputFile = new File(outputFileName);
			outputFile.createNewFile();
			this.csvWriter = new FileWriter(outputFile);
			this.csvWriter.write("#ID"+SEPERATOR+"FR"+SEPERATOR+"X"+SEPERATOR+"Y"+SEPERATOR+"Z"+"\n");
			//csvWriter.close();
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
	public void notifyAfterMobsim(AfterMobsimEvent event) {
		try {
			for ( List<String> t : this.trajectories.values()) {
				for (String c : t) {
					this.csvWriter.write(c);
				}
			}
			
			this.csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleEvent(CAAgentMoveEvent event) {
		Pedestrian pedestrian = event.getPedestrian();
		double to_x = MathUtility.convertGridCoordinate(event.getTo_x());
		double to_y = MathUtility.convertGridCoordinate(event.getTo_y());
		if (to_x >= 0 && to_x <= this.maxX && to_y >= 0 && to_y <= this.maxY) {
			List<String> container = this.trajectories.get(event.getPedestrian().getId());
			if (container == null) {
				container = new ArrayList<>();
				this.trajectories.put(event.getPedestrian().getId(), container);
			}
			container.add( ""+pedestrian.getID()+SEPERATOR+(int)(0.5+event.getRealTime()/TIME_STEP_SIZE)+SEPERATOR+to_x+SEPERATOR+to_y+SEPERATOR+0.+"\n");
		}
	}

	@Override
	public void handleEvent(CAAgentExitEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(CAAgentMoveToOrigin event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(CAAgentEnterEnvironmentEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		// TODO Auto-generated method stub
		
	}





}

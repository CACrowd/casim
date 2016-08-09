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

package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.utility.FileUtility;

import java.io.*;
import java.util.ArrayList;

public class MarkerConfiguration {
	private ArrayList<Start> starts;
	private ArrayList<Destination> destinations;
	private ArrayList<GridPoint> destinationsCells;
	
	public MarkerConfiguration(){
		starts = new ArrayList<Start>();
		destinations = new ArrayList<Destination>();
		destinationsCells = new ArrayList<GridPoint>();
	}
	
	public MarkerConfiguration(ArrayList<Start> starts, ArrayList<Destination> destinations){
		this.starts = starts;
		this.destinations = destinations;
	}
	
	public MarkerConfiguration(String path) {
		this();
		try {
			loadConfiguration(path);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	//TODO tests
	public Destination getDestination(int destinationID){
		return destinations.get(destinationID);
	}
	
	public void addDestination(Destination destination){
		destinations.add(destination);
		destinationsCells.addAll(destination.getCells());
	}
	
	public void addStart(Start start){
		starts.add(start);
	}
	
	public ArrayList<Start> getStarts(){
		return starts;
	}
	
	public ArrayList<Destination> getDestinations(){
		return destinations;
	}

	public void saveConfiguration(String path) throws IOException{
		path = path+"/markers";
		FileUtility.deleteDirectory(new File(path));
		new File(path+"/starts").mkdirs();
		new File(path+"/destinations").mkdirs();
		FileOutputStream fout;
        ObjectOutputStream oos;
        
        for (int i = 0; i < starts.size(); i++) {
        	File file = new File(path+"/starts/start_"+i+".ser");
			file.createNewFile();
        	fout = new FileOutputStream(path+"/starts/start_"+i+".ser", false);
        	oos = new ObjectOutputStream(fout);
        	oos.writeObject(starts.get(i));
        	oos.close();
        }
		
		for (int i = 0; i < destinations.size(); i++) {
			if (destinations.get(i) instanceof FinalDestination){
				File file = new File(path+"/destinations/tacticalDestination_"+i+".ser");
				file.createNewFile();
	            fout = new FileOutputStream(path+"/destinations/tacticalDestination_"+i+".ser", false);
			}else{
				File file = new File(path+"/destinations/destination_"+i+".ser");
				file.createNewFile();
	            fout = new FileOutputStream(path+"/destinations/destination_"+i+".ser", false);
			}
	            oos = new ObjectOutputStream(fout);
	            oos.writeObject(destinations.get(i));
	            oos.close();
        }
		
	}
	
	public void loadConfiguration(String path) throws IOException, ClassNotFoundException{
		path = path+"/markers";
		int countFiles = new File(path+"/starts").listFiles().length;
		FileInputStream streamIn; 
		ObjectInputStream ois;
		
		for (int i = 0; i < countFiles; i++) {			
			streamIn = new FileInputStream(path+"/starts/start_"+i+".ser");
			ois = new ObjectInputStream(streamIn);
            addStart((Start) ois.readObject());
            ois.close();
        }
		
		countFiles = new File(path+"/destinations").listFiles().length;
		for (int i = 0; i < countFiles; i++) {
			try{
				streamIn = new FileInputStream(path+"/destinations/destination_"+i+".ser");
				ois = new ObjectInputStream(streamIn);
				addDestination((Destination) ois.readObject());
			}catch(IOException e){
				streamIn = new FileInputStream(path+"/destinations/tacticalDestination_"+i+".ser");
				ois = new ObjectInputStream(streamIn);
				addDestination((FinalDestination) ois.readObject());
			}
			ois.close();
        }
	}

	public ArrayList<GridPoint> getBorderCells() {
		return destinationsCells;
	}
}

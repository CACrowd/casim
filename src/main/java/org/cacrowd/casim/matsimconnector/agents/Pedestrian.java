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

package org.cacrowd.casim.matsimconnector.agents;

import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.utility.IdUtility;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.PedestrianGrid;
import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;
import org.cacrowd.casim.pedca.environment.markers.DelayedDestination;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.environment.markers.FinalDestination;
import org.cacrowd.casim.pedca.utility.NeighbourhoodUtility;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;

public class Pedestrian extends Agent {

	public Double lastTimeCheckAtExit = null;
	private Id<Pedestrian> Id;
	private QVehicle vehicle;
	private TransitionArea transitionArea;
	private boolean destinationReached;
	private FinalDestination originMarker;
	private Neighbourhood nextStepNeighbourhood;
	private int timeToCrossDestination;		

	public Pedestrian(Agent agent, QVehicle vehicle, TransitionArea transitionArea){
		this(agent.getID(),agent.getPosition(), agent.getDestination(), agent.getContext());
		this.vehicle = vehicle;
		destinationReached = false;
		this.transitionArea = transitionArea;
		this.originMarker = transitionArea.getReferenceDestination();
		this.timeToCrossDestination = 0;
		enterTransitionArea(getPosition());
	}
	
	private Pedestrian(int Id, GridPoint position, Destination destination, Context context) {
		super(Id, position, destination, context);
		generateId();
	}
	
	@Override
	public void percept(){
		if (destinationReached && transitionArea!=null && getStaticFFValue(getPosition())==0.){
			exit();
		}
		if (timeToCrossDestination > 0)
			--timeToCrossDestination;
	}
	
	private void perceptIfFinalDestination(double now) {
		if (timeToCrossDestination == 0 && getStaticFFValue(getPosition())==0. && transitionArea == null){
			destinationReached = true;
			if(destination instanceof DelayedDestination)
				timeToCrossDestination = ((DelayedDestination)destination).waitingTimeForCrossing(now)+1;
			if (now < Constants.CA_TEST_END_TIME){
				calculateNextStepNeighbourhood();
			}
		}else if(transitionArea == null && timeToCrossDestination == 0){
			destinationReached = false;
		}
	}

	public void move(double now){
		if (transitionArea!=null){
			if(!getPosition().equals(getNewPosition())){
				transitionArea.moveTo(this, getNewPosition());
			}
			updateHeading();
			setPosition(getNewPosition());
		}else {
			if (!isWaitingToSwap() && destinationReached && !getNewPosition().equals(getPosition()))
				lastTimeCheckAtExit = now;
			super.move();
			perceptIfFinalDestination(now);
		}
	}

	@Override
	protected Double getStaticFFValue(GridPoint gridPoint) {
		if (transitionArea!=null)
			return getTransitionAreaFieldValue(gridPoint);
		else
			return super.getStaticFFValue(gridPoint);
	}

	public double getTransitionAreaFieldValue(GridPoint gridPoint){
		return transitionArea.getSFFValue(gridPoint,destinationReached);
	}
	
	public void setTransitionArea(TransitionArea transitionArea){
		this.transitionArea = transitionArea;
	}
	
	private void generateId(){
		Id = IdUtility.createPedestrianId(super.getID());
	}

	public Id<Pedestrian> getId(){
		return Id;
	}
	
	public QVehicle getVehicle(){
		return vehicle;
	}
	
	@Override
	protected void setPosition(GridPoint position){
		super.setPosition(position);
	}
	
	public boolean isEnteringEnvironment(){
		return transitionArea != null && getStaticFFValue(getPosition())==0. && !destinationReached;
	}
	
	public boolean isDestinationReached(){
		return destinationReached;
	}
	
	public boolean hasLeftEnvironment(){
		return destinationReached && transitionArea != null;
	}
	
	public void moveToUniverse(){
		if (transitionArea != null)
			leaveTransitionArea();
		else
			leavePedestrianGrid();
	}

	//TODO TEST!!!
	private void calculateNextStepNeighbourhood() {
		Neighbourhood neighbourhood = NeighbourhoodUtility.calculateMooreNeighbourhood(position);//getPedestrianGrid().getFreePositions(originMarker.getCells()));
		nextStepNeighbourhood = new Neighbourhood();
		for (GridPoint neighbour : neighbourhood.getObjects()){
			if (neighbour.getX()>0)
				neighbour.setX(neighbour.getX()%getPedestrianGrid().getColumns());
			else
				neighbour.setX(neighbour.getX()+getPedestrianGrid().getColumns());
			if (originMarker.getCells().contains(neighbour))
				nextStepNeighbourhood.add(neighbour);
		}
		if (nextStepNeighbourhood.size() == 0)
			nextStepNeighbourhood.add(getPosition());
	}	
	
	public void refreshDestination() {
		Id<Link> linkId = vehicle.getDriver().getCurrentLinkId();
		int destinationId = IdUtility.linkIdToDestinationId(linkId);
		this.destination = context.getMarkerConfiguration().getDestination(destinationId);
		this.destinationReached = false;
	}
	
	public void moveToEnvironment(){
		GridPoint nextPosition = transitionArea.convertTAPosToEnvPos(getPosition());
		leaveTransitionArea();
		enterPedestrianGrid(nextPosition);
	}
	
	public void moveToTransitionArea(TransitionArea transitionArea) {
		setTransitionArea(transitionArea);
		GridPoint translatedPosition = transitionArea.convertEnvPosToTAPos(getPosition());
		leavePedestrianGrid();
		enterTransitionArea(translatedPosition);
	}

	private void enterTransitionArea(GridPoint position) {
		transitionArea.addPedestrian(position, this);
		setPosition(position);
	}
	
	private void leaveTransitionArea(){
		transitionArea.removePedestrian(getPosition(), this);
		transitionArea = null;
		setPosition(null);
	}
	
	public PedestrianGrid getUsedPedestrianGrid(){
		if (transitionArea != null)
			return transitionArea;
		return getPedestrianGrid();
	}
	
	@Override
	public Neighbourhood getNeighbourhood(){
		//has to yield position
		if(stopOnStairs() || timeToCrossDestination > 0){
			Neighbourhood result = new Neighbourhood();
			result.add(new GridPoint(position.getX(), position.getY()));
			return result;
		}
		if (nextStepNeighbourhood != null && transitionArea == null){
			Neighbourhood result = nextStepNeighbourhood;
			nextStepNeighbourhood = null;
			return result;
		}
		if (transitionArea != null){
			nextStepNeighbourhood = null;
			return transitionArea.getNeighbourhood(getPosition());
		}else
			return super.getNeighbourhood();
	}
	
	private boolean stopOnStairs(){
		return !Constants.stopOnStairs && isOnStairs();
	}
	
	public boolean isCrossingDestination() {
		return timeToCrossDestination > 1;
	}
	
	public int getTimeToCrossDest(){
		return timeToCrossDestination;
	}
	
	private boolean isOnStairs(){
		try{
			Id<Link> currentLinkId = this.getVehicle().getDriver().getCurrentLinkId();
			if (currentLinkId != null){
				String linkId = currentLinkId.toString();
				for (String stairId : Constants.stairsLinks){
					if (stairId.equals(linkId))
						return true;
				}
			}
			return false;
		}catch(NullPointerException e){
			return false;
		}		
	}
	
	protected boolean canSwap(GridPoint neighbour, PedestrianGrid pedestrianGrid) {
		if (destinationReached && pedestrianGrid.containsPedestrian(neighbour) && transitionArea == null){
		 	return ((Pedestrian)pedestrianGrid.getPedestrian(neighbour)).destinationReached;
		}			
		return super.canSwap(neighbour, pedestrianGrid);
	}
	/*
	protected boolean isInFrontCell(GridPoint neighbour) {
		if (nextStepNeighbourhood != null)
			return false;
		return super.isInFrontCell(neighbour);
	}*/
	
	@Override
	protected boolean checkOccupancy(GridPoint neighbour) {
		//TODO FIX THE PROBLEM WITH TRANSITION AREAS
		if(isAtEnvironmentBorder(neighbour)){
			if (transitionArea != null)
				return checkOccupancy(neighbour,transitionArea) || getPedestrianGrid().isOccupied(transitionArea.convertTAPosToEnvPos(neighbour));						//transitionArea.isOccupied(neighbour) || getPedestrianGrid().isOccupied(transitionArea.convertTAPosToEnvPos(neighbour));//checkOccupancy(neighbour,transitionArea) || checkOccupancy(transitionArea.convertTAPosToEnvPos(neighbour),getPedestrianGrid());
			else{
				TransitionArea neighbourTA = getFinalDestination(neighbour).getTransitionArea();
				return neighbourTA.isOccupied(neighbourTA.convertEnvPosToTAPos(neighbour)) || checkOccupancy(neighbour,getPedestrianGrid());//checkOccupancy(neighbourTA.convertEnvPosToTAPos(neighbour),neighbourTA) || checkOccupancy(neighbour,getPedestrianGrid());
			}
		}		
		return checkOccupancy(neighbour, getUsedPedestrianGrid());
	}
	
	private FinalDestination getFinalDestination(GridPoint neighbour) {
		FinalDestination result = null;
		for (Destination dest : getContext().getMarkerConfiguration().getDestinations())
			if (dest instanceof FinalDestination && dest.getCells().contains(neighbour)){
				result = (FinalDestination)dest;
				break;
			}
		return result;
	}

	//TODO TEST!!!
	public boolean isAtEnvironmentBorder(GridPoint position) {
		if (transitionArea != null){
			return transitionArea.isAtBorder(position);
		} else
			return getFinalDestination(position)!=null;
	}

	/**
	 * These two methods return the "real" position of the agent in the environment,
	 * considering transitionAreas as extension of the grid at its borders
	 * */
	public GridPoint getRealPosition(){
		if (transitionArea != null){
			GridPoint pos = new GridPoint(getPosition().getX(),getPosition().getY());
			return transitionArea.convertTAPosToEnvPos(pos);
		}
		return getPosition();
	}
	
	public GridPoint getRealNewPosition(){
		if (transitionArea != null){
			GridPoint pos = new GridPoint(getNewPosition().getX(),getNewPosition().getY());
			return transitionArea.convertTAPosToEnvPos(pos);
		}
		return getNewPosition();
	}
	
	public FinalDestination getOriginMarker(){
		return originMarker;
	}
}

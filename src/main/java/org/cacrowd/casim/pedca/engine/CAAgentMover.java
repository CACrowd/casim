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

package org.cacrowd.casim.pedca.engine;

import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.agents.Population;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.utility.Constants;

public class CAAgentMover implements AgentMover {
    private final Population population;
//       private CAEngine engineCA;
//       private EventsManager eventManager;
    //	private boolean stairs = true;

    public CAAgentMover(Context context) {//}, EventsManager eventManager) {
        this.population = context.getPopulation();
//   		this.engineCA = engineCA;
        Constants.stopOnStairs = false;
    }

    @Override
    public void step(double now) {
        Constants.stopOnStairs = !Constants.stopOnStairs;
        //		stairs = !stairs;
        for (int index = 0; index < population.size(); index++) {
            Agent pedestrian = population.getPedestrian(index);
            if (pedestrian.isArrived()) {
                //Log.log(pedestrian.toString()+" Exited.");
                delete(pedestrian);
                index--;
            } else {
                GridPoint oldPosition = pedestrian.getPosition();
                //				if (stairs && isOnStairs(pedestrian)){
                //					eventManager.processEvent(new CAAgentMoveEvent(now, pedestrian, oldPosition, oldPosition));
                //					continue;
                //				}
                GridPoint newPosition = pedestrian.getNewPosition();
                moveAgent(pedestrian, now);
//   				if (Constants.VIS)
//   					eventManager.processEvent(new CAAgentMoveEvent(now, pedestrian, oldPosition, newPosition));
//   				if(!pedestrian.isWaitingToSwap() && pedestrian.isEnteringEnvironment()){
//   					moveToCA(pedestrian, now);
//   				}else if (!pedestrian.isWaitingToSwap() && pedestrian.isDestinationReached() && !pedestrian.isCrossingDestination() && !pedestrian.hasLeftEnvironment()){
//   					Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
//   					if (engineCA.getCALink(nextLinkId) != null){
//   						changeLinkInsideEnvironment(pedestrian, now);
//   					}
//   					else if(now>= Constants.CA_TEST_END_TIME){
//   						//TODO check if the outlink can host pedestrians coming from the CA environment
//   						moveToQ(pedestrian, now);
//   					}
//   				}
            }
        }
    }

    public void moveAgent(Agent pedestrian, double now) {
//   		Double pedestrianTravelTime = pedestrian.lastTimeCheckAtExit;
        pedestrian.move();
//   		if (pedestrianTravelTime != null && pedestrian.lastTimeCheckAtExit != pedestrianTravelTime){
//   			pedestrianTravelTime = pedestrian.lastTimeCheckAtExit - pedestrianTravelTime;
//   			eventManager.processEvent(new CAAgentMoveToOrigin(now, pedestrian, pedestrianTravelTime));
//   		}
    }

    private void delete(Agent pedestrian) {
//   		pedestrian.re;
        population.remove(pedestrian);
    }

//   	private void moveToCA(Pedestrian pedestrian, double time) {
//   		//Log.log(pedestrian.toString() + " Moving inside Pedestrian Grid");
//   		Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
//   		Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
//   		engineCA.getQCALink(currentLinkId).notifyMoveOverBorderNode(pedestrian.getVehicle(), nextLinkId);
//   		pedestrian.getVehicle().getDriver().notifyMoveOverNode(nextLinkId);
//
//   		eventManager.processEvent(new CAAgentEnterEnvironmentEvent(time, pedestrian));
//
//   		pedestrian.moveToEnvironment();
//   	}

//   	private void moveToQ(Pedestrian pedestrian, double time) {
//   		//Log.log(pedestrian.toString()+" Moving to CAQLink.");
//   		Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
//   		Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
//   		CAQLink lowResLink = engineCA.getCAQLink(nextLinkId);
//   		lowResLink.notifyMoveOverBorderNode(pedestrian.getVehicle(), currentLinkId);
//   		pedestrian.getVehicle().getDriver().notifyMoveOverNode(nextLinkId);
//   		lowResLink.addFromUpstream(pedestrian.getVehicle());
//
//   		eventManager.processEvent(new CAAgentLeaveEnvironmentEvent(time, pedestrian));
//
//   		TransitionArea transitionArea = lowResLink.getTransitionArea();
//   		pedestrian.moveToTransitionArea(transitionArea);
//   	}
//
//   	private void changeLinkInsideEnvironment(Pedestrian pedestrian, double time) {
//   		//Log.log(pedestrian.toString()+" changing CALink.");
//   		Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
//   		Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
//   		CALink nextLinkCA = engineCA.getCALink(nextLinkId);
//   		nextLinkCA.notifyMoveOverBorderNode(pedestrian.getVehicle(), currentLinkId);
//   		pedestrian.getVehicle().getDriver().notifyMoveOverNode(nextLinkId);
//
//   		eventManager.processEvent(new CAAgentChangeLinkEvent(time, pedestrian, currentLinkId.toString(), nextLinkId.toString()));
//
//   		// CHANGE THE COLOR OF THE AGENT LC
//   		//eventManager.processEvent(new CAAgentLeaveEnvironmentEvent(time, pedestrian));
//
//   		// CHANGE THE DESTINATION OF THE AGENT
//   		pedestrian.refreshDestination();
//       }

    //	private boolean isOnStairs(Pedestrian pedestrian){
    //		try{
    //			Id<Link> currentLinkId = pedestrian.getVehicle().getDriver().getCurrentLinkId();
    //			if (currentLinkId != null){
    //				String linkId = currentLinkId.toString();
    //				for (String stairId : Constants.stairsLinks){
    //					if (stairId.equals(linkId))
    //						return true;
    //				}
    //			}
    //			return false;
    //		}catch(NullPointerException e){
    //			return false;
    //		}
    //	}
}

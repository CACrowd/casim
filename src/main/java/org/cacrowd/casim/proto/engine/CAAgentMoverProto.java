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

package org.cacrowd.casim.proto.engine;


import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.agents.Population;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.AgentMover;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;

/**
 * Created by laemmel on 09/08/16.
 */
public class CAAgentMoverProto implements AgentMover {
    private final Population population;
    private final CAEngine engine;

    public CAAgentMoverProto(CAEngine caEngine, Context context, CAEnvironment env) {
        this.population = context.getPopulation();
        this.engine = caEngine;
        Constants.stopOnStairs = false;
    }

    @Override
    public void step(double now) {
        Constants.stopOnStairs = !Constants.stopOnStairs; //TODO what does this mean? move only every other sim step?

        for (int index = 0; index < population.size(); index++) {
            Agent pedestrian = population.getPedestrian(index);
            if (pedestrian.isArrived()) {
                //Log.log(pedestrian.toString()+" Exited.");
                delete(pedestrian);
                index--;
            } else {
                GridPoint oldPosition = pedestrian.getPosition(); //TODO transition area like in matsimconnector (getRealPosition)
//				if (stairs && isOnStairs(pedestrian)){
//					eventManager.processEvent(new CAAgentMoveEvent(now, pedestrian, oldPosition, oldPosition));
//					continue;
//				}
                GridPoint newPosition = pedestrian.getNewPosition();//TODO transition area like in matsimconnector (getRealNewPosition)
                moveAgent(pedestrian, now);

                //TODO: VIS
//                if (Constants.VIS)
//                    eventManager.processEvent(new CAAgentMoveEvent(now, pedestrian, oldPosition, newPosition));


//                if (!pedestrian.isWaitingToSwap() && pedestrian.isEnteringEnvironment()) {
//                    moveToCA(pedestrian, now);
//                } else if (!pedestrian.isWaitingToSwap() && pedestrian.isDestinationReached() && !pedestrian.isCrossingDestination() && !pedestrian.hasLeftEnvironment()) {
//                    Id<Link> nextLinkId = pedestrian.getVehicle().getDriver().chooseNextLinkId();
//                    if (engineCA.getCALink(nextLinkId) != null) {
//                        changeLinkInsideEnvironment(pedestrian, now);
//                    } else if (now >= Constants.CA_TEST_END_TIME) {
//                        //TODO check if the outlink can host pedestrians coming from the CA environment
//                        moveToQ(pedestrian, now);
//                    }
//                }
            }
        }
    }

    public void moveAgent(Agent pedestrian, double now) {
//        Double pedestrianTravelTime = pedestrian.lastTimeCheckAtExit;
//        pedestrian.move(now);
//        if (pedestrianTravelTime != null && pedestrian.lastTimeCheckAtExit != pedestrianTravelTime){
//            pedestrianTravelTime = pedestrian.lastTimeCheckAtExit - pedestrianTravelTime;
//            eventManager.processEvent(new CAAgentMoveToOrigin(now, pedestrian, pedestrianTravelTime));
//        }
    }

    private void delete(Agent pedestrian) {
        //TODO: rm from transition area
//        pedestrian.moveToUniverse();
        population.remove(pedestrian);
    }
}

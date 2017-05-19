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

package org.cacrowd.casim.pedca.agents;

import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.FloorFieldsGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.PedestrianGrid;
import org.cacrowd.casim.pedca.environment.grid.WeightedCell;
import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.pedca.utility.DirectionUtility;
import org.cacrowd.casim.pedca.utility.DirectionUtility.Heading;
import org.cacrowd.casim.pedca.utility.Lottery;
import org.cacrowd.casim.pedca.utility.MathUtility;

import java.util.ArrayList;

//operational level
public class Agent extends PhysicalObject {

    protected final Context context;
    private final int Id;
    private final Tactic tactic;

    private GridPoint nextpos;
    private Heading heading;
    private boolean arrived;

    private boolean wantToSwap;
    private int stepToPerformSwap;
    private boolean hasToSwap;

    public Agent(int Id, GridPoint position, Tactic tactic, Context context) {
        this.Id = Id;
        this.position = nextpos = position;
        this.context = context;
        this.tactic = tactic;
        arrived = false;
        //TODO ENSURE THAT X IS NEVER ASSUMED DURING THE SIMULATION
        heading = Heading.X;
        wantToSwap = false;
        stepToPerformSwap = 0;
        hasToSwap = false;
    }

    public void updateChoice() {
        if (isWaitingToSwap()) {
            stepToPerformSwap--;
            hasToSwap = stepToPerformSwap == 0;
        } else {
            percept();
            if (!isArrived()) {
                ArrayList<WeightedCell> probabilityValues = evaluate();
                choose(probabilityValues);
                checkBidirectionalSwitch();
            }
        }
    }

    private void checkBidirectionalSwitch() {
        if (!nextpos.equals(position) && getUsedPedestrianGrid().isOccupied(nextpos))
            wantToSwap = true;
    }

    public PedestrianGrid getUsedPedestrianGrid() {
        return getPedestrianGrid();
    }

    public int calculateStepToPerformSwap() {
        double pedestrianDensity = calculatePerceivedDensity();
        double delay_seconds = Math.pow(pedestrianDensity * .61, 1.45) * 0.4;//Math.pow(pedestrianDensity*.61,1.43)*0.39;
        double delay = delay_seconds / Constants.STEP_DURATION;
        int result = (int) delay;//Constants.STEP_FOR_BIDIRECTIONAL_SWAPPING;
        double probability = delay - result;
        if (Lottery.simpleExtraction(probability))
            result++;
        return result;
    }

    private double calculatePerceivedDensity() {
        return getUsedPedestrianGrid().getPedestrianDensity(position);
        /* THIS IS FOR THE SHIFT OF THE DENSITY PERCEPTION
        GridPoint shiftedPosition = getShiftedPosition();
		double pedestrianDensity = getUsedPedestrianGrid().getPedestrianDensity(shiftedPosition);
		return pedestrianDensity;*/
    }

    public GridPoint getShiftedPosition() {
        GridPoint direction = DirectionUtility.convertHeadingToGridPoint(this.heading);
        System.out.println(this.Id + "pos: " + position.toString());
        GridPoint shiftedPosition = new GridPoint(position.getX() + direction.getX(), position.getY() + direction.getY());
        System.out.println(this.Id + "shifted: " + shiftedPosition.toString());
        if (this.getUsedPedestrianGrid().isWalkable(shiftedPosition))
            return shiftedPosition;
        else
            return position;
    }

    public void startBidirectionalSwitch(int stepToPerformSwap) {
        wantToSwap = false;
        this.stepToPerformSwap = stepToPerformSwap;
    }

    public int getStepToPerformSwap() {
        return stepToPerformSwap;
    }

    private void percept() {
        if (tactic.exit(position))
            exit();
    }

    private ArrayList<WeightedCell> evaluate() {
        double myPositionValue = getStaticFFValue(position);
        double neighbourValue;
        double occupation;
        double probabilitySum = 0.0;

        Neighbourhood neighbourhood = getNeighbourhood();
        ArrayList<WeightedCell> probabilityValues = new ArrayList<WeightedCell>();

        for (int index = 0; index < neighbourhood.size(); index++) {
            GridPoint neighbour = neighbourhood.get(index);
            neighbourValue = getStaticFFValue(neighbour);

            occupation = 0.0;
            if ((!neighbour.equals(position)) && checkOccupancy(neighbour))
                occupation = 1.0;

            double p = utilityFunction(myPositionValue, neighbourValue, occupation);


            probabilitySum += p;
            probabilityValues.add(new WeightedCell(neighbour, p));
        }
        Lottery.normalizeProbabilities(probabilityValues, probabilitySum);
        return probabilityValues;
    }

    private double utilityFunction(double myPositionValue, double neighbourValue, double occupation) {
        double utilityValue = Math.pow(Math.E, Constants.KS * (myPositionValue - neighbourValue));
        utilityValue = utilityValue * (1 - (Constants.PHI * occupation));                //FORMULA => Math.pow(Math.E, Constants.KS*(MyPositionValue - neighbourValue))*epsilon*(1-(Constants.PHI*n));
        return utilityValue;
    }

    private void choose(ArrayList<WeightedCell> probabilityValues) {
        WeightedCell winningCell = Lottery.pickWinner(probabilityValues);
        if (winningCell == null)
            nextpos = position;
        else {
            nextpos = new GridPoint(winningCell.getX(), winningCell.getY());
        }
    }

    public void move() {
        if (!isWaitingToSwap() && !hasToSwap) {
            if (!position.equals(nextpos)) {
                getPedestrianGrid().moveTo(this, nextpos);
            }
            updateHeading();
            setPosition(nextpos);
        } else if (!isWaitingToSwap()) {
            getUsedPedestrianGrid().moveToWithoutShadow(this, nextpos);
            setPosition(nextpos);
            hasToSwap = false;
        }
    }

    private boolean checkOccupancy(GridPoint neighbour) {
        return checkOccupancy(neighbour, getPedestrianGrid());
    }

    private boolean checkOccupancy(GridPoint neighbour, PedestrianGrid pedestrianGrid) {
        return pedestrianGrid.isOccupied(neighbour) && !canSwap(neighbour, pedestrianGrid);
    }

    private boolean canSwap(GridPoint neighbour, PedestrianGrid pedestrianGrid) {
        if (pedestrianGrid.containsPedestrian(neighbour)) {
            Agent neighbourAgent = pedestrianGrid.getPedestrian(neighbour);
            int deltaFF = (int) Math.round(neighbourAgent.getStaticFFValue(neighbour) - neighbourAgent.getStaticFFValue(position));
            return deltaFF > 0;
        }
        return false;

        /**
         if (pedestrianGrid.containsPedestrian(neighbour) && isInFrontCell(neighbour)){
         Heading neighbourHeading = pedestrianGrid.getPedestrian(neighbour).getHeading();
         return counterflowHeading(neighbourHeading);
         }
         return false;*/
    }

    /*
    protected boolean isInFrontCell(GridPoint neighbour) {
        //TODO MANAGE nextStepNeighbourhood
        GridPoint deltaPosition = MathUtility.gridPointDifference(neighbour, position);
        return DirectionUtility.convertGridPointToHeading(deltaPosition).equals(heading);
    }

    private boolean counterflowHeading(Heading neighbourHeading) {
        if (getHeading().equals(Heading.X))
            return false;
        GridPoint directionSum = MathUtility.gridPointSum(DirectionUtility.convertHeadingToGridPoint(getHeading()), DirectionUtility.convertHeadingToGridPoint(neighbourHeading));
        return directionSum.getX() == 0 && directionSum.getY() == 0;
    }
    */
    private void updateHeading() {
        if (!position.equals(nextpos)) {
            GridPoint positionDelta = MathUtility.gridPointDifference(nextpos, position);
            heading = DirectionUtility.convertGridPointToHeading(positionDelta);
        }
//		else{
//			//heading = Lottery.extractHeading();
//		}
    }

    protected void setPosition(GridPoint position) {
        this.position = position;
    }

    public void exit() {
        arrived = true;
    }

    public boolean delete() {
        context.getPedestrianGrid().removePedestrian(this.position, this);
        return true;
    }

    public void enterPedestrianGrid(GridPoint position) {
        getPedestrianGrid().addPedestrian(position, this);
        setPosition(position);
    }

    public void leavePedestrianGrid() {
        getPedestrianGrid().removePedestrian(getPosition(), this);
        setPosition(null);
    }

    public boolean isArrived() {
        return arrived;
    }

    public void revertChoice() {
        nextpos = position;
    }

    public void revertWillingToSwap() {
        revertChoice();
        wantToSwap = false;
    }

    private double getStaticFFValue(GridPoint gridPoint) {
        return this.tactic.getStaticFFValue(gridPoint);
//        return getStaticFF().getCellValue(destination.getLevel(), gridPoint);
    }

    private FloorFieldsGrid getStaticFF() {
        return context.getFloorFieldsGrid();
    }


    private PedestrianGrid getPedestrianGrid() {
        return context.getPedestrianGrid();
    }

    public Neighbourhood getNeighbourhood() {
        return getStaticFF().getNeighbourhood(position);
    }

    public int getID() {
        return Id;
    }

//    public Destination getTacticalDestination() {
//        return destination;
//    }

    public Context getContext() {
        return context;
    }

    public GridPoint getNewPosition() {
        return nextpos;
    }

    protected void setNewPosition(GridPoint position) {
        this.nextpos = position;
    }

    public Heading getHeading() {
        return heading;
    }

    public boolean isWillingToSwap() {
        return wantToSwap;
    }

    public boolean isWaitingToSwap() {
        return stepToPerformSwap > 0;
    }

    public String toString() {
        return "Pedestrian " + getID();
    }
}

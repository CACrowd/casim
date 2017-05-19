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

package org.cacrowd.casim.pedca.environment.markers;

import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;

import java.util.ArrayList;

public class FinalDestination extends TacticalDestination {

    private static final long serialVersionUID = 1L;
    //for the transition area
    private int rotation = -1;
    private GridPoint environmentRef;
    private TransitionArea transitionArea;
    private GridPoint environmentCenter;

    public FinalDestination(Coordinate coordinate, ArrayList<GridPoint> cells, GridPoint environmentCenter) {
        super(coordinate, cells, false);
        this.environmentCenter = environmentCenter;
        calculateRotationAndRef();
    }

    //TODO test!!!
    private void calculateRotationAndRef() {
        if (getCells().size() > 1) {
            GridPoint first = getCells().get(0);
            GridPoint second = getCells().get(1);
            GridPoint last = getCells().get(getCells().size() - 1);
            if (first.getX() == second.getX()) {
                if (first.getX() <= environmentCenter.getX()) {
                    rotation = 0;
                    environmentRef = first;
                } else {
                    rotation = 180;
                    environmentRef = last;
                }
            } else if (first.getY() == second.getY()) {
                if (first.getY() <= environmentCenter.getY()) {
                    rotation = 90;
                    environmentRef = last;
                } else if (first.getY() != 0) {//equal y but at the end of the environment grid
                    rotation = 270;
                    environmentRef = first;
                }
            }
        } else {
            environmentRef = getCells().get(0);
            if (environmentRef.getX() == 0)
                rotation = 0;
            else
                rotation = 180;
        }
    }

    public GridPoint getEnvironmentRef() {
        return environmentRef;
    }

    public int getRotation() {
        return rotation;
    }

    //TODO CLEAN THIS
    public TransitionArea getTransitionArea() {
        return transitionArea;
    }

    public void setTransitionArea(TransitionArea transitionArea) {
        this.transitionArea = transitionArea;
    }
}

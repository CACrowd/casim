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

package org.cacrowd.casim.pedca.environment.network;

import java.io.Serializable;

public class Coordinate implements Serializable {

    private static final long serialVersionUID = 1L;
    private double x;
    private double y;

//	public Coordinate(GridPoint gp){
//		this.x=gp.getX()*Constants.CA_CELL_SIDE;
//		this.y=gp.getY()*Constants.CA_CELL_SIDE;
//	}

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

}

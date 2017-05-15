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

package org.cacrowd.casim.pedca.environment.grid;

public class WeightedCell {
    private int x;
    private int y;
    private double p;

    public WeightedCell(GridPoint gp, double p) {
        this.x = gp.getX();
        this.y = gp.getY();
        this.p = p;
    }

    public double getP() {
        return p;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return "(" + x + "," + y + "," + p + ")";
    }
}

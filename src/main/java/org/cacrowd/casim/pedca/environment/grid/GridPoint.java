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

import java.io.Serializable;

public class GridPoint implements Serializable {

    private static final long serialVersionUID = 1L;
    private int x;
    private int y;

    public GridPoint(int col, int row) {
        setX(col);
        setY(row);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public final boolean equals(Object gp) {
        return y == ((GridPoint) gp).getY() && x == ((GridPoint) gp).getX();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public String toString() {
        return getY() + " " + getX();
    }

}

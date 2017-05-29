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

package org.cacrowd.casim.pedca.environment.network;

public class CAEdge {
    private CANode n1;
    private CANode n2;
    private double length;
    private boolean isStairs;

    public CAEdge(CANode n1, CANode n2, double ffDistance, boolean isStairs) {
        this.n1 = n1;
        this.n2 = n2;
        this.length = ffDistance;
        this.isStairs = isStairs;
    }

    public double getLength() {
        return length;
    }

    public CANode getN1() {
        return n1;
    }

    public CANode getN2() {
        return n2;
    }

    public boolean isStairs() {
        return isStairs;
    }

    @Override
    public String toString() {
        String result = super.toString() + "\n";
        result += "FROM: " + n1.getCoordinate() + " to " + n2.getCoordinate() + "\n";
        result += "LENGTH: " + length;
        return result;
    }
}

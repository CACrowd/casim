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

package org.cacrowd.casim.utility.rasterizer;


import org.cacrowd.casim.pedca.utility.Constants;

/**
 * Created by laemmel on 12/07/16.
 */
public final class Edge {


    private final double x0, x1, y0, y1;
    private final double dx;
    private final ScanlineRasterizer.EdgeType edgeType;
    private final int id;
    private double currentX;

    private boolean rightOfWallOpen = true;

    public Edge(int id, double x0, double y0, double x1, double y1, ScanlineRasterizer.EdgeType edgeType) {
        this.id = id;
        this.edgeType = edgeType;
        if (y0 < y1) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
        } else {
            this.x1 = x0;
            this.y1 = y0;
            this.x0 = x1;
            this.y0 = y1;
            rightOfWallOpen = false;
        }

        dx = Constants.CELL_SIZE * (this.x1 - this.x0) / (this.y1 - this.y0);
        currentX = this.x0;
    }

    public boolean isRightOfWallOpen() {
        return rightOfWallOpen;
    }

    public double getY0() {
        return this.y0;
    }

    public double getCurrentX() {
        return this.currentX;
    }

    public void incrCurrentX() {
        this.currentX += this.dx;
    }

    public double getY1() {
        return y1;
    }

    public double getX0() {
        return this.x0;
    }

    public double getX1() {
        return this.x1;
    }

    public ScanlineRasterizer.EdgeType getEdgeType() {
        return this.edgeType;
    }

    public int getId() {
        return id;
    }
}

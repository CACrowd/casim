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


import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.utility.Constants;

import java.util.*;

/**
 * Scanline algorithm to rasterize polygonal environment
 * Created by laemmel on 10/07/16.
 */
public class Rasterizer {


    private static final Logger log = Logger.getLogger(Rasterizer.class);
    private final EnvironmentGrid grid;

    public Rasterizer(EnvironmentGrid grid) {
        this.grid = grid;
    }

    public static int getColorCode(EdgeType edgeType) {
        switch (edgeType) {
            case WALL:
                return 0;
            case TRANSITION:
                return -3;
            case TRANSITION_INTERNAL:
                return -2;
            default:
                throw new RuntimeException("Unknown Cell-Type:" + edgeType);
        }
    }

    public void rasterize(Collection<Edge> edges) {

        LinkedList<Edge> edgeTable = new LinkedList<>(edges);

        cleanUpET(edgeTable);

        edgeTable.sort((o1, o2) -> o1.getY0() < o2.getY0() ? -1 : 1);

        LinkedList<Edge> activeEdgeTable = new LinkedList<>();
        ListIterator<Edge> it = edgeTable.listIterator();
        int row = 0;//scanline nr
        int mxRow = this.grid.getRows();

        for (; row <= mxRow; row++) {

            updateAET(activeEdgeTable, row);
            while (it.hasNext()) {
                Edge e = it.next();
                int eRow = this.grid.y2Row(e.getY0());
                if (eRow == row) {
                    activeEdgeTable.add(e);
                } else {
                    it.previous();
                    break;
                }
            }
            activeEdgeTable.sort((o1, o2) -> o1.getCurrentX() < o2.getCurrentX() ? -1 : 1);
            setPixels(activeEdgeTable, row);

        }

        traceEdges(edges);

    }

    private void traceEdges(Collection<Edge> edgeTable) {


        for (Edge e : edgeTable) {  //1st walls
            if (e.getEdgeType() != EdgeType.WALL) {
                continue;
            }
            traceEdge(e);
        }

        for (Edge e : edgeTable) { //then transitions
            if (e.getEdgeType() == EdgeType.WALL) {
                continue;
            }
            traceEdge(e);
        }
    }

    private void traceEdge(Edge e) {
        double x0 = e.getX0();
        double x1 = e.getX1();
        double y0 = e.getY0();
        double y1 = e.getY1();

        double dx = Constants.CELL_SIZE * (x1 - x0) / (y1 - y0);
        double dy = Constants.CELL_SIZE * (y1 - y0) / (x1 - x0);

        int colorCode = getColorCode(e.getEdgeType());

        if (Math.abs(dx) <= Math.abs(dy)) {
            traceRowWise(y0, y1, x0, dx, e, colorCode);
        } else {
            traceColWise(x0, x1, y0, y1, dy, colorCode, e);

        }
    }

    private void traceColWise(double x0, double x1, double y0, double y1, double dy, int colorCode, Edge e) {
        if (x1 < x0) {
            double tmp = x1;
            x1 = x0;
            x0 = tmp;
            tmp = y1;
            y0 = tmp;
        }

        int frCol = grid.x2Col(x0);
        int toCol = grid.x2Col(x1);
        double currentY = y0;
        int oldRow = -1;
        int oldCol = frCol;
        for (int col = frCol; col <= toCol; col++) {
            int row = grid.y2Row(currentY);
            grid.setCellValue(row, col, colorCode);
            if (e.getEdgeType() == EdgeType.TRANSITION || e.getEdgeType() == EdgeType.TRANSITION_INTERNAL) {
                if (oldCol != col && oldRow != row) {
                    grid.setCellValue(row, oldCol, colorCode);
                }
            }
            oldRow = row;
            oldCol = col;
            currentY += dy;
        }

        if (e.getEdgeType() == EdgeType.TRANSITION || e.getEdgeType() == EdgeType.TRANSITION_INTERNAL) {//test if transition is connected to a wall, close it if not
            int row = grid.y2Row(currentY - dy);

            closeTransition(toCol, row, colorCode);
            closeTransition(frCol, grid.y2Row(y0), colorCode);

        }

    }

    private void traceRowWise(double y0, double y1, double x0, double dx, Edge e, int colorCode) {
        int frRow = grid.y2Row(y0);
        int toRow = grid.y2Row(y1);
        double currentX = x0;
        int oldRow = frRow;
        int oldCol = -1;
        for (int row = frRow; row <= toRow; row++) {
            int col = grid.x2Col(currentX);


            grid.setCellValue(row, col, colorCode);
            if (e.getEdgeType() == EdgeType.TRANSITION || e.getEdgeType() == EdgeType.TRANSITION_INTERNAL) {
                if (oldCol != col && oldRow != row) {
                    grid.setCellValue(oldRow, col, colorCode);

                }
            }
            oldRow = row;
            oldCol = col;

            currentX += dx;
        }

        if (e.getEdgeType() == EdgeType.TRANSITION || e.getEdgeType() == EdgeType.TRANSITION_INTERNAL) {//test if transition is connected to a wall, close it if not
            int col = grid.x2Col(currentX - dx);
            closeTransition(col, toRow, colorCode);
            closeTransition(grid.x2Col(x0), frRow, colorCode);

        }

    }

    private void closeTransition(int col, int row, int colorCode) {
        if (!connectsToWall(col, row)) {
//            if (col == 0 && row == 24) {
//                System.out.println("Gotcha!");
//            }
            if (connectsToWall(col - 1, row)) {
                grid.setCellValue(row, col - 1, colorCode);
                return;
            }
            if (connectsToWall(col + 1, row)) {
                grid.setCellValue(row, col + 1, colorCode);
                return;
            }
            if (connectsToWall(col, row - 1)) {
                grid.setCellValue(row - 1, col, colorCode);
                return;
            }
            if (connectsToWall(col, row + 1)) {
                grid.setCellValue(row + 1, col, colorCode);
                return;
            }

            throw new RuntimeException("Could not connect transition to wall!");
        }

    }

    private boolean connectsToWall(int col, int row) {
        if (col < 0 || col > grid.getColumns() || row < 0 || row > grid.getRows()) {
            return false;
        }

//        if (grid.getCellValue(row,col) == Constants.ENV_OBSTACLE) {
//            return true;
//        }

        if (col == 0) {
            return false;
        }
        if (grid.getCellValue(row, col - 1) == org.cacrowd.casim.pedca.utility.Constants.ENV_OBSTACLE) {
            return true;
        }
        if (col == grid.getColumns()) {
            return false;
        }
        if (grid.getCellValue(row, col + 1) == org.cacrowd.casim.pedca.utility.Constants.ENV_OBSTACLE) {
            return true;
        }
        if (row == 0) {
            return false;
        }
        if (grid.getCellValue(row - 1, col) == org.cacrowd.casim.pedca.utility.Constants.ENV_OBSTACLE) {
            return true;
        }

        if (row == grid.getRows()) {
            return false;
        }

        return grid.getCellValue(row + 1, col) == org.cacrowd.casim.pedca.utility.Constants.ENV_OBSTACLE;

    }

    private void cleanUpET(List<Edge> edgeTable) {
        Iterator<Edge> it = edgeTable.iterator();
        while (it.hasNext()) {
            Edge next = it.next();
            if (next.getEdgeType() == EdgeType.TRANSITION_INTERNAL) {
                it.remove();
            } else {
                int row1 = this.grid.y2Row(next.getY0());
                int row2 = this.grid.y2Row(next.getY1());
                if (row1 == row2) {
                    it.remove();
                }
            }
        }
    }

    private void setPixels(LinkedList<Edge> activeEdgeTable, int row) {

        if (activeEdgeTable.size() == 0) {
            return;
        }

        ListIterator<Edge> it = activeEdgeTable.listIterator();


        Edge first = it.next();
        while (it.hasNext()) {
            Edge second = it.next();
            int col = this.grid.x2Col(first.getCurrentX());
            int nextCol = this.grid.x2Col(second.getCurrentX());
            for (int i = col; i <= nextCol; i++) {

                if (this.grid.getCellValue(row, i) == -1) {
                    this.grid.setCellValue(row, i, 0);
                }
            }
            if (it.hasNext()) {
                first = it.next();

            }


        }
    }

    private void updateAET(LinkedList<Edge> activeEdgeTable, int row) {
        Iterator<Edge> it = activeEdgeTable.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            int toRow = this.grid.y2Row(e.getY1());
            if (toRow <= row) {
                it.remove();
            } else {
                e.incrCurrentX();
            }
        }
    }

    public enum EdgeType {TRANSITION, TRANSITION_INTERNAL, WALL}

}

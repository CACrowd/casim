package proto.geom;
/****************************************************************************/
// casim, cellular automaton simulation for multi-destination pedestrian
// crowds; see https://github.com/CACrowd/casim
// Copyright (C) 2016 CACrowd and contributors
/****************************************************************************/
//
//   This file is part of casim.
//   casim is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 2 of the License, or
//   (at your option) any later version.
//
/****************************************************************************/


import matsimconnector.utility.Constants;
import org.apache.log4j.Logger;
import pedca.environment.grid.EnvironmentGrid;

import java.util.*;

/**
 * Scanline algorithm to rasterize polygonal environment
 * Created by laemmel on 10/07/16.
 */
public class Rasterizer {


    private static final Logger log = Logger.getLogger(Rasterizer.class);

    public enum EdgeType {TRANSITION, TRANSITION_INTERNAL, WALL}

    private final EnvironmentGrid grid;

    public Rasterizer(EnvironmentGrid grid) {
        this.grid = grid;
    }

    public void rasterize(Collection<Edge> edges) {

        LinkedList<Edge> edgeTable = new LinkedList<>(edges);

        cleanUpET(edgeTable);

        Collections.sort(edgeTable, (o1, o2) -> o1.getY0() < o2.getY0() ? -1 : 1);

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
            Collections.sort(activeEdgeTable, (o1, o2) -> o1.getCurrentX() < o2.getCurrentX() ? -1 : 1);
            setPixels(activeEdgeTable, row);

        }

        traceEdges(edges);

    }

    private void traceEdges(Collection<Edge> edgeTable) {

        for (Edge e : edgeTable) {
            if (e.getEdgeType() == EdgeType.WALL) {
                continue;
            }
            double x0 = e.getX0();
            double x1 = e.getX1();
            double y0 = e.getY0();
            double y1 = e.getY1();

            double dx = Constants.CA_CELL_SIDE * (x1 - x0) / (y1 - y0);
            double dy = Constants.CA_CELL_SIDE * (y1 - y0) / (x1 - x0);

            int colorCode = getColorCode(e.getEdgeType());

            if (Math.abs(dx) <= Math.abs(dy)) {
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

//                if (e.getEdgeType() == EdgeType.TRANSITION || e.getEdgeType() == EdgeType.TRANSITION_INTERNAL) {
//
//                }
            } else {
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
            }
        }
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

    public static int getColorCode(EdgeType edgeType) {
        switch (edgeType) {
            case WALL:
                return 0;
            case TRANSITION:
                return -2;
            case TRANSITION_INTERNAL:
                return -2;
            default:
                throw new RuntimeException("Unknown Cell-Type:" + edgeType);
        }
    }

}

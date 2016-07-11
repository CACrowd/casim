package proto.engine;
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
import proto.HybridSimProto;

import java.util.*;

/**
 * Scanline algorithm to rasterize polygonal environment
 * Created by laemmel on 10/07/16.
 */
public class Rasterizer {

    private static final Logger log = Logger.getLogger(Rasterizer.class);

    enum Kind {TRANSITION, WALL}

    private final EnvironmentGrid grid;

    public Rasterizer(EnvironmentGrid grid) {
        this.grid = grid;
    }

    public void rasterize(HybridSimProto.Environment environment) {

        List<Edge> edgeTable = createEdgeTable(environment);
        Collections.sort(edgeTable, (o1, o2) -> o1.y0 < o2.y0 ? -1 : 1);

        LinkedList<Edge> activeEdgeTable = new LinkedList<>();
        ListIterator<Edge> it = edgeTable.listIterator();
        int row = 0;//scanline nr
        int mxRow = this.grid.getRows();

        for (; row <= mxRow; row++) {

            updateAET(activeEdgeTable, row);
            while (it.hasNext()) {
                Edge e = it.next();
                int eRow = this.grid.y2Row(e.y0);
                if (eRow == row) {
                    activeEdgeTable.add(e);
                } else {
                    it.previous();
                    break;
                }
            }

            Collections.sort(activeEdgeTable, (o1, o2) -> o1.currentX < o2.currentX ? -1 : 1);
            setPixels(activeEdgeTable, row);

        }

    }

    private void setPixels(LinkedList<Edge> activeEdgeTable, int row) {

        if (activeEdgeTable.size() == 0) {
            return;
        }

        Iterator<Edge> it = activeEdgeTable.iterator();
        Edge first = it.next();
        while (it.hasNext()) {
            Edge next = it.next();
            if (first != null) {
                int col = this.grid.x2Col(first.currentX);
                int nextCol = this.grid.x2Col(next.currentX);
                if (nextCol != col) {
                    for (int i = col; i < nextCol; i++) {

                        this.grid.setCellValue(row, i, 0);
                    }
                    first = null;
                } else {
                    first = next;
                }
            } else {
                first = next;
            }
        }
    }

    private void updateAET(LinkedList<Edge> activeEdgeTable, int row) {
        Iterator<Edge> it = activeEdgeTable.iterator();
        while (it.hasNext()) {
            Edge e = it.next();
            int toRow = this.grid.y2Row(e.y1);
            if (toRow <= row) {
                it.remove();
            } else {
                e.currentX += e.dx;
            }
        }
    }

    private List<Edge> createEdgeTable(HybridSimProto.Environment environment) {
        List<Edge> edgeTable = new ArrayList<>();
        for (HybridSimProto.Room r : environment.getRoomList()) {
            for (HybridSimProto.Subroom s : r.getSubroomList()) {
                double x0 = Double.NaN;
                double y0 = Double.NaN;
                for (HybridSimProto.Polygon p : s.getPolygonList()) {
                    Iterator<HybridSimProto.Coordinate> it = p.getCoordinateList().iterator();
                    while (it.hasNext()) {
                        HybridSimProto.Coordinate c = it.next();
                        if (Double.isNaN(x0)) {
                            x0 = c.getX();
                            y0 = c.getY();
                        } else {
                            Edge edge = new Edge(x0, y0, c.getX(), c.getY(), Kind.WALL);
                            edgeTable.add(edge);
                            if (it.hasNext()) {
                                x0 = c.getX();
                                y0 = c.getY();
                            } else {
                                x0 = Double.NaN;
                                y0 = Double.NaN;
                            }
                        }
                    }

                }
            }
        }
        log.debug("Extracted: " + edgeTable.size() + " wall edges");
        for (HybridSimProto.Transition tr : environment.getTransitionList()) {
            Edge edge = new Edge(tr.getVert1().getX(), tr.getVert1().getY(), tr.getVert2().getX(), tr.getVert2().getY(), Kind.TRANSITION);
            Edge cEdge = new Edge(edge);
            edgeTable.add(edge);
            edgeTable.add(cEdge);
        }
        log.debug("Edge table size: " + edgeTable.size());
        return edgeTable;
    }

    private final static class Edge {


        final double x0, x1, y0, y1;
        final double dx;

        double currentX;

        final Kind kind;

        public Edge(double x0, double y0, double x1, double y1, Kind kind) {
            this.kind = kind;
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
            }

            dx = Constants.CA_CELL_SIDE * (this.x1 - this.x0) / (this.y1 - this.y0);
            currentX = this.x0;
        }

        public Edge(Edge edge) {
            x0 = edge.x0;
            x1 = edge.x1;
            y0 = edge.y0;
            y1 = edge.y1;
            kind = edge.kind;
            dx = edge.dx;
            currentX = this.x0;
        }


    }
}

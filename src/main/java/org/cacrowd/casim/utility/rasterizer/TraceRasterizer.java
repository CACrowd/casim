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


import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.vividsolutions.jts.geom.Envelope;
import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfigurationImpl;
import org.cacrowd.casim.pedca.environment.markers.TacticalDestination;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.visualizer.VisualizerEngine;

import java.util.*;

import static org.cacrowd.casim.scenarios.EnvironmentGenerator.generateCoordinates;

/**
 * Scanline algorithm to rasterize polygonal environment
 * Created by laemmel on 10/07/16.
 */
public class TraceRasterizer implements Rasterizer {


    private static final Logger log = Logger.getLogger(TraceRasterizer.class);
    private final Map<Integer, List<GridPoint>> protoDestinations = new LinkedHashMap<>();
    @Inject
    Context context;

    @Inject
    SimulationObserver observer;

    private EnvironmentGrid grid;
    private MarkerConfiguration markerConfiguration;

//    public ScanlineRasterizer(EnvironmentGrid grid) {
//        this.grid = grid;
//    }

    public static int getColorCode(ScanlineRasterizer.EdgeType edgeType) {
        switch (edgeType) {
            case WALL:
                return -1;
            case TRANSITION:
                return -2;
            case TRANSITION_INTERNAL:
                return 0;
//            case TRANSITION_HOLDOVER:
//                return 0;
            default:
                throw new RuntimeException("Unknown Cell-Type:" + edgeType);
        }
    }

    public static void main(String[] args) {
        LinkedList<Edge> et = new LinkedList<>();
        Edge e0 = new Edge(0, 0, 0, 0, 2.4, ScanlineRasterizer.EdgeType.WALL);
        et.add(e0);
        Edge e0a = new Edge(1, 0.4, 0.41, 0.4, 1.99, ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0a);

        Edge e0b = new Edge(2, 2., 0.41, 2., 1.99, ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0b);

        Edge e0c = new Edge(3, 14., 0.41, 14., 1.99, ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0c);

        Edge e0d = new Edge(4, 15.6, 0.41, 15.6, 1.99, ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0d);

        Edge e1 = new Edge(0, 0, 2.4, 16, 2.4, ScanlineRasterizer.EdgeType.WALL);
        et.add(e1);
        Edge e2 = new Edge(0, 16, 2.4, 16, 0, ScanlineRasterizer.EdgeType.WALL);
        et.add(e2);
        Edge e3 = new Edge(0, 16, 0, 0, 0, ScanlineRasterizer.EdgeType.WALL);
        et.add(e3);

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(SimulationObserver.class).to(VisualizerEngine.class);
            }
        });

        injector.getInstance(TraceRasterizer.class).buildContext(et);

        injector.getInstance(SimulationObserver.class).observerEnvironmentGrid();

    }

    @Override
    public void buildContext(Collection<Edge> edges) {

        Envelope e = new Envelope();
        edges.forEach(edge -> {
            e.expandToInclude(edge.getX0(), edge.getY0());
            e.expandToInclude(edge.getX1(), edge.getY1());
        });

        int rows = (int) (e.getHeight() / Constants.CELL_SIZE) + 1;
        int cols = (int) (e.getWidth() / Constants.CELL_SIZE) + 1;
        this.grid = new EnvironmentGrid(rows, cols, e.getMinX(), e.getMinY());
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid.setCellValue(row, col, 0);
            }
        }

        context.setEnvironmentGrid(grid);
        traceEdges(edges);
        generateDestinations();
        context.initialize(grid, markerConfiguration);


    }

    private void generateDestinations() {
        markerConfiguration = new MarkerConfigurationImpl();
        this.protoDestinations.entrySet().stream().map(e ->
                new TacticalDestination(e.getKey(), generateCoordinates(e.getValue()), e.getValue(), grid.isStairsBorder(e.getValue().get(0))))
                .forEach(markerConfiguration::addTacticalDestination);
    }


    private void traceEdges(Collection<Edge> edgeTable) {


        for (Edge e : edgeTable) {  //1st pass walls
            if (e.getEdgeType() != ScanlineRasterizer.EdgeType.WALL) {
                continue;
            }
            traceEdge(e);
        }

        for (Edge e : edgeTable) { //then transitions
            if (e.getEdgeType() == ScanlineRasterizer.EdgeType.WALL) {
                continue;
            }
            traceEdge(e);
        }

//        for (Edge e : edgeTable) {  //2nd pass walls
//            if (e.getEdgeType() != ScanlineRasterizer.EdgeType.WALL) {
//                continue;
//            }
//            traceEdge(e);
//        }

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
            if (e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION || e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL) {
                if (oldCol != col && oldRow != row) {
                    protoDestinations.computeIfAbsent(e.getId(), k -> new ArrayList<>()).add(new GridPoint(oldCol, row));
                }
                protoDestinations.computeIfAbsent(e.getId(), k -> new ArrayList<>()).add(new GridPoint(col, row));

            }
            if (oldCol != col && oldRow != row) {
                grid.setCellValue(row, oldCol, colorCode);
            }
            oldRow = row;
            oldCol = col;
            currentY += dy;
        }

        if (e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION || e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL) {//test if transition is connected to a wall, close it if not
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
            if (e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION || e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL) {
                if (oldCol != col && oldRow != row) {
                    protoDestinations.computeIfAbsent(e.getId(), k -> new ArrayList<>()).add(new GridPoint(col, oldRow));
                }
                protoDestinations.computeIfAbsent(e.getId(), k -> new ArrayList<>()).add(new GridPoint(col, row));

            }
            if (oldCol != col && oldRow != row) {
                grid.setCellValue(oldRow, col, colorCode);
            }
            oldRow = row;
            oldCol = col;

            currentX += dx;
        }

        if (e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION || e.getEdgeType() == ScanlineRasterizer.EdgeType.TRANSITION_INTERNAL) {//test if transition is connected to a wall, close it if not
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
        if (grid.getCellValue(row, col - 1) == Constants.ENV_OBSTACLE) {
            return true;
        }
        if (col == grid.getColumns()) {
            return false;
        }
        if (grid.getCellValue(row, col + 1) == Constants.ENV_OBSTACLE) {
            return true;
        }
        if (row == 0) {
            return false;
        }
        if (grid.getCellValue(row - 1, col) == Constants.ENV_OBSTACLE) {
            return true;
        }

        if (row == grid.getRows()) {
            return false;
        }

        return grid.getCellValue(row + 1, col) == Constants.ENV_OBSTACLE;

    }

}

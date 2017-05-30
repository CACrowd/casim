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

package org.cacrowd.casim.visualizer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.agents.ActivePopulation;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.DensityGrid;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.utility.SimulationObserver;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Singleton
public class VisualizerEngine implements SimulationObserver {


    private static final Logger log = Logger.getLogger(VisualizerEngine.class);

    private final TreeMap<Double, Color> densityColorRamp = new TreeMap<>();
    private final Map<Integer, Color> agentsColors = new HashMap<>();


    private final Control keyControl;
    private final double dT;
    //    private final FrameSaver fs = new FrameSaver("/Users/laemmel/tmp/vis/", "png", 0);
    private final FrameSaver fs = null;

    private final Visualizer vis = new Visualizer(fs);
    @Inject
    Context context;
    @Inject
    InfoBox infoBox;
    private long lastUpdate;

    @Inject
    public VisualizerEngine() {
        this.keyControl = new Control(this.vis.zoomer, 20, fs);
        this.vis.addKeyControl(this.keyControl);
        this.dT = Constants.STEP_DURATION;
        init();
    }

    private void init() {
        Color green = new Color();
        green.a = 128;
        green.r = 0;
        green.g = 255;
        green.b = 0;
        this.densityColorRamp.put(0., green);
        Color yellow = new Color();
        yellow.a = 128;
        yellow.r = 255;
        yellow.g = 255;
        yellow.b = 0;
        this.densityColorRamp.put(1., yellow);
        Color red = new Color();
        red.a = 128;
        red.r = 255;
        red.g = 0;
        red.b = 0;
        this.densityColorRamp.put(3., red);
        Color blue = new Color();
        blue.a = 255;
        blue.r = 0;
        blue.g = 0;
        blue.b = 255;
        this.densityColorRamp.put(7., blue);
        this.densityColorRamp.put(Double.POSITIVE_INFINITY, blue);
    }

//    @Override
//    public void observeTransitionAreas(Map<GridPoint,TransitionArea> areaMap){
//        EnvironmentGrid environmentGrid = context.getEnvironmentGrid();
//        for (Map.Entry<GridPoint,TransitionArea> entry : areaMap.entrySet())  {
//            int r = entry.getValue().hashCode()%255;
//            int g = entry.getValue().toString().hashCode()%255; int b = entry.getValue().pointList.hashCode()%255; int a = 255; boolean fill = true;
//
//            Coordinate c = environmentGrid.gridPoint2Coordinate(entry.getKey());
//            vis.addRectStatic(c.getX() - Constants.CELL_SIZE / 2, c.getY() + Constants.CELL_SIZE / 2, Constants.CELL_SIZE, Constants.CELL_SIZE, r, g, b, a, 0, fill);
//        }
//    }


    @Override
    public void observerEnvironmentGrid() {
        this.vis.addAdditionalDrawer(infoBox);
        EnvironmentGrid environmentGrid = context.getEnvironmentGrid();
        for (int row = 0; row < environmentGrid.getRows(); row++) {
            for (int col = 0; col < environmentGrid.getColumns(); col++) {
                Coordinate c = environmentGrid.rowCol2Coordinate(row, col);
                Integer color = environmentGrid.getCellValue(row, col);
                int r, g, b, a;
                boolean fill;
                if (color == 0) {
                    r = 0;
                    g = 0;
                    b = 0;
                    a = 128;
                    fill = false;
                } else if (color == -1) {
                    r = 0;
                    g = 0;
                    b = 0;
                    a = 128;
                    fill = true;
                } else {
                    r = 128;
                    g = 128;
                    b = 128;
                    a = 128;
                    fill = true;
                }
                vis.addRectStatic(c.getX() - Constants.CELL_SIZE / 2, c.getY() + Constants.CELL_SIZE / 2, Constants.CELL_SIZE, Constants.CELL_SIZE, r, g, b, a, 0, fill);
//                vis.addTextStatic(c.getX(), c.getY(), "(" + col + " " + row + ")", 150);
            }
        }

        vis.update(0);

    }

    @Override
    public void observerDensityGrid() {
        DensityGrid densityGridGrid = context.getDensityGrid();
//        FloorFieldsGrid ff = context.getFloorFieldsGrid();

        for (int row = 0; row < densityGridGrid.getRows(); row++) {
            for (int col = 0; col < densityGridGrid.getColumns(); col++) {
                Coordinate c = densityGridGrid.rowCol2Coordinate(row, col);
                double density = densityGridGrid.getDensityAt(new GridPoint(col, row));
//                double density = ff.getCellValue(1,new GridPoint(col,row)) ;

                int r, g, b, a;
                boolean fill;
                if (Double.isNaN(density) || density <= 0.01) {
                    continue;
                } else {
                    Map.Entry<Double, Color> floor = this.densityColorRamp.floorEntry(density);
                    Map.Entry<Double, Color> ceiling = this.densityColorRamp.ceilingEntry(density);

//                     log.info("density: " + density);
                    double range = ceiling.getKey() - floor.getKey();
                    double wCeiling = density - floor.getKey();
                    double wFloor = ceiling.getKey() - density;


                    r = (int) ((wFloor * floor.getValue().r + wCeiling * ceiling.getValue().r) / range);
                    g = (int) ((wFloor * floor.getValue().g + wCeiling * ceiling.getValue().g) / range);
                    b = (int) ((wFloor * floor.getValue().b + wCeiling * ceiling.getValue().b) / range);
                    a = (int) ((wFloor * floor.getValue().a + wCeiling * ceiling.getValue().a) / range);
                    fill = true;
                }
                vis.addRect(c.getX() - Constants.CELL_SIZE / 2, c.getY() + Constants.CELL_SIZE / 2, Constants.CELL_SIZE, Constants.CELL_SIZE, r, g, b, a, 0, fill);
                double prntDens = ((int) (density * 100. + 0.5)) / 100.;
                vis.addText(c.getX(), c.getY(), Double.toString(prntDens), 150);
            }
        }
    }

    @Override
    public void observePopulation() {
        ActivePopulation population = context.getPopulation();
        population.getPedestrians().forEach(this::draw);
        double time = context.getTimeOfDay();
        update(time);

    }


    private void update(double time) {
        this.keyControl.awaitPause();
        this.keyControl.awaitScreenshot();
        this.keyControl.update(time);
        long timel = System.currentTimeMillis();

        long last = this.lastUpdate;
        long diff = timel - last;
        if (diff < this.dT * 1000 / this.keyControl.getSpeedup()) {
            long wait = (long) (this.dT * 1000 / this.keyControl.getSpeedup() - diff);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.vis.update(time);
        this.lastUpdate = System.currentTimeMillis();
    }

    private void draw(Agent a) {
        GridPoint gp = a.getPosition();
        Coordinate c = context.getPedestrianGrid().gridPoint2Coordinate(gp);

        Color color = this.agentsColors.computeIfAbsent(a.getID(), k -> {
            Color col = new Color();
            col.a = 255;
            col.r = 0;
            //some pseudo random colors
            col.g = 192 + a.hashCode() % 128 - 64;
            col.b = 192 + a.getPosition().hashCode() % 128 - 64;
            if (a.getID() < 0) {
                col.r = col.g;
                col.g = 0;
            }
            return col;
        });

        vis.addCircle(c.getX(), c.getY(), .2f, color.r, color.g, color.b, color.a, 0, true);

    }

    private static final class Color {
        int r, g, b, a;
    }
}

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

package org.cacrowd.casim.visualizer;

import com.google.inject.Inject;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.agents.Population;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.utility.SimulationObserver;

public class VisualizerEngine implements SimulationObserver {

    private final Control keyControl;
    private final double dT;
    private final Visualizer vis = new Visualizer(null);
    @Inject
    Context context;
    @Inject
    InfoBox infoBox;
    private long lastUpdate;

    @Inject
    public VisualizerEngine() {
        this.keyControl = new Control(this.vis.zoomer, 20, null);
        this.vis.addKeyControl(this.keyControl);
        this.dT = Constants.STEP_DURATION;

    }

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
            }
        }

        vis.update(0);

    }

    @Override
    public void observePopulation() {
        Population population = context.getPopulation();
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
        vis.addCircle(c.getX(), c.getY(), .2f, 255, 0, 0, 255, 0, true);

    }
}

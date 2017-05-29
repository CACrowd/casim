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

package org.cacrowd.casim.hybridsim.testclient;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.proto.HybridSimProto;
import org.cacrowd.casim.visualizer.Control;
import org.cacrowd.casim.visualizer.FrameSaver;
import org.cacrowd.casim.visualizer.Visualizer;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class VisualizerDummyEngine {


    private static final Logger log = Logger.getLogger(VisualizerDummyEngine.class);

    private final Map<Integer, Color> agentsColors = new HashMap<>();


    private final Control keyControl;
    private final double dT;
    //    private final FrameSaver fs = new FrameSaver("/Users/laemmel/tmp/vis/", "png", 0);
    private final FrameSaver fs = null;

    private final Visualizer vis = new Visualizer(fs);

    private long lastUpdate;

    @Inject
    public VisualizerDummyEngine() {
        this.keyControl = new Control(this.vis.zoomer, 20, fs);
        this.vis.addKeyControl(this.keyControl);
        this.dT = Constants.STEP_DURATION;
    }


    public void drawEnvironment(HybridSimProto.Scenario sc) {
        for (HybridSimProto.Edge edge : sc.getEdgesList()) {
            int r, g, b, a;
            if (edge.getType() == HybridSimProto.Edge.Type.OBSTACLE) {
                r = 0;
                g = 0;
                b = 0;
                a = 255;
                vis.addLineStatic(edge.getC0().getX(), edge.getC0().getY(), edge.getC1().getX(), edge.getC1().getY(), r, g, b, a, 0);
            } else if (edge.getType() == HybridSimProto.Edge.Type.TRANSITION) {
                r = 0;
                g = 0;
                b = 255;
                a = 255;
                vis.addDashedLineStatic(edge.getC0().getX(), edge.getC0().getY(), edge.getC1().getX(), edge.getC1().getY(), r, g, b, a, 0, .15, .5);
            } else {
                r = 255;
                g = 0;
                b = 0;
                a = 255;
                vis.addLineStatic(edge.getC0().getX(), edge.getC0().getY(), edge.getC1().getX(), edge.getC1().getY(), r, g, b, a, 0);
            }
        }
        updateTime(0);

    }

    public void drawTrajectories(HybridSimProto.Trajectories trajectories) {
        for (HybridSimProto.Trajectory trajectory : trajectories.getTrajectoriesList()) {
            Color color = this.agentsColors.computeIfAbsent(trajectory.getId(), k -> {
                Color col = new Color();
                col.a = 255;
                col.r = 0;
                //some pseudo random colors
                col.g = 192 + trajectory.hashCode() % 128 - 64;
                col.b = 192 + trajectory.toString().hashCode() % 128 - 64;
                if (trajectory.getId() < 0) {
                    col.r = col.g;
                    col.g = 0;
                }
                return col;
            });

            vis.addCircle(trajectory.getX(), trajectory.getY(), .2f, color.r, color.g, color.b, color.a, 0, true);
        }
    }


    public void updateTime(double time) {
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


    private static final class Color {
        int r, g, b, a;
    }
}

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
import org.cacrowd.casim.pedca.context.Context;
import org.matsim.core.utils.misc.Time;
import processing.core.PConstants;

public class InfoBox implements VisDebuggerAdditionalDrawer, VisDebuggerOverlay {


    @Inject
    Context context;
    private String runInfo2 = null;
    private String runInfo1 = null;
    private String runInfo0;


    @Override
    public void draw(Visualizer p) {

    }

    @Override
    public void drawText(Visualizer p) {
        p.strokeWeight(2);
        p.fill(0, 0, 0, 235);
        p.stroke(222, 222, 222, 255);
        int round = 10;


        float ts = 18;
        float x = 5 + round;
        float y = 5 + round + ts;
        p.textSize(ts);
//		PVector cv = p.zoomer.getCoordToDisp(new PVector((float)(li.tx+p.offsetX),(float)-(li.ty+p.offsetY)));
//		p.fill(0,0,0,255);
//		float w = p.textWidth(li.text);

        double t = context.getTimeOfDay();
        String tm = Time.writeTime(t, Time.TIMEFORMAT_HHMMSS);
        String stm = "time: " + tm;
        float w = p.textWidth(stm);
        if (runInfo0 != null && runInfo1 != null && runInfo2 != null) {
            p.rect(5, 5, 5 + 15 + w + round, 5 + round + ts + round + ts + ts / 2 + ts + ts / 2 + ts + ts / 2, round);
        } else {
            p.rect(5, 5, 5 + 15 + w + round, 5 + round + ts + round, round);
        }

        p.fill(255);
        p.textAlign(PConstants.LEFT);
        p.text(stm, x, y);
//		double sph = this.speedup > .98 ? Math.round(this.speedup) : this.speedup;
//		String tt = Integer.toString(ttt);
//		String dec = Integer.toString((int)((this.speedup-ttt)*100));
        if (runInfo1 != null && runInfo2 != null) {
            p.text(runInfo0, x, y + ts + ts / 2);
            p.text(runInfo1, x, y + ts + ts / 2 + ts + ts / 2);
            p.text(runInfo2, x, y + ts + ts / 2 + ts + ts / 2 + ts + ts / 2);
        }

    }


    public void setRunInfo1(String runInfo) {
        this.runInfo1 = runInfo;
    }

    public void setRunInfo2(String runInfo) {
        this.runInfo2 = runInfo;
    }

    public void setRunInfo0(String runInfo0) {
        this.runInfo0 = runInfo0;
    }
}

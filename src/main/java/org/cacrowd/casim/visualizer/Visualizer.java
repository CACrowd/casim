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

import org.gicentre.utils.move.ZoomPan;
import processing.core.PApplet;
import processing.core.PVector;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by laemmel on 07/03/2017.
 */
public class Visualizer extends PApplet {

    private final JFrame fr;
    private final java.util.List<Text> elementsTextStatic = Collections
            .synchronizedList(new ArrayList<Text>());
    private final java.util.List<Object> newElements = new ArrayList<>();
    private final java.util.List<Text> newElementsText = new ArrayList<>();
    private final java.util.List<Object> elementsStatic = Collections
            .synchronizedList(new ArrayList<Object>());
    private final java.util.List<VisDebuggerAdditionalDrawer> additionalDrawers = Collections
            .synchronizedList(new ArrayList<VisDebuggerAdditionalDrawer>());
    // ZoomPan zoomer;
    private final FrameSaver fs;

    public ZoomPan zoomer;
    double time2;
    int dummy = 0;
    private java.util.List<Object> elements = Collections
            .synchronizedList(new ArrayList<Object>());
    private java.util.List<Text> elementsText = Collections
            .synchronizedList(new ArrayList<Text>());
    private Control keyControl;
    // private final FrameSaver fs = null;
    private String it;

    public Visualizer(FrameSaver fs) {
        this.fs = fs;
        this.fr = new JFrame();
        this.fr.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // this.fr.setSize(1024,788);
        //		this.fr.setSize(1024, 788);
//        this.fr.setSize(1280, 740);
        this.fr.setSize(950, 740);
        // this.fr.setSize(720,740);
        JPanel compositePanel = new JPanel();
        compositePanel.setLayout(new OverlayLayout(compositePanel));

        this.fr.add(compositePanel, BorderLayout.CENTER);

        compositePanel.add(this);
        compositePanel.setEnabled(true);
        compositePanel.setVisible(true);

        this.zoomer = new ZoomPan(this);// ,this.recorder); // Initialise the
        // zoomer.

        this.init();
        frameRate(90);
        // size(1024, 768);
        // size(1024, 768);
        this.fr.setVisible(true);
    }


    @Override
    public void setup() {
        //		size(1024, 768);
//        size(1280, 720);
        size(950, 720);
        // size(720,720);
        background(0);

    }

    @Override
    public void draw() {
        Font f = this.getFont();

        boolean recording = false;
        ZoomPan old = null;
        if (this.keyControl != null && this.keyControl.isScreenshotRequested()
                && this.keyControl.isOneObjectWaitingAtScreenshotBarrier()) {
            beginRecord(PDF,
                    "/Users/laemmel/Desktop/" + "sim2d_screenshot_at_"
                            + this.time2 + "_" + System.currentTimeMillis()
                            + ".pdf");
            recording = true;
            old = this.zoomer;
            this.zoomer = new ZoomPan(this, this.recorder);
            this.zoomer.setZoomScale(old.getZoomScale());
            this.zoomer
                    .setPanOffset(old.getPanOffset().x, old.getPanOffset().y);

        }

        pushMatrix();
        if (recording) {
            this.recorder.pushMatrix();
        }
        // This enables zooming/panning and should be in the draw method.
        this.zoomer.transform();
        background(255);

        java.util.List<PVector> coords = new ArrayList<PVector>();
        for (int x = 0; x <= this.width; x += 128) {
            for (int y = 0; y <= this.height + 128; y += 128) {
                PVector d = this.zoomer.getDispToCoord(new PVector(x, y));
                //				 coords.add(d);
            }
        }


        synchronized (this.additionalDrawers) {
            for (VisDebuggerAdditionalDrawer d : this.additionalDrawers) {
                d.draw(this);
            }
        }

        popMatrix();
        if (recording) {
            this.recorder.popMatrix();
        }
        synchronized (this.additionalDrawers) {
            for (VisDebuggerAdditionalDrawer d : this.additionalDrawers) {
                if (d instanceof VisDebuggerOverlay) {
                    continue;
                }
                d.drawText(this);
            }
        }
        pushMatrix();
        if (recording) {
            this.recorder.pushMatrix();
        }
        this.zoomer.transform();

        strokeWeight((float) (2 / this.zoomer.getZoomScale()));
        strokeCap(ROUND);

        synchronized (this.elementsStatic) {
            for (Object el : this.elementsStatic) {
                if (el instanceof Line) {
                    drawLine((Line) el);
                } else if (el instanceof Circle) {
                    drawCircle((Circle) el);
                } else if (el instanceof Polygon) {
                    drawPolygon((Polygon) el);
                } else if (el instanceof Rect) {
                    drawRect((Rect) el);
                }
            }
        }

        for (Object obj : this.elements) {
            if (obj instanceof Circle) {
                drawCircle((Circle) obj);
            } else if (obj instanceof Line) {
                drawLine((Line) obj);
            } else if (obj instanceof Triangle) {
                drawTriangle((Triangle) obj);
            } else if (obj instanceof Rect) {
                drawRect((Rect) obj);
            } else if (obj instanceof Polygon) {
                drawPolygon((Polygon) obj);
            }
        }

        popMatrix();
        if (recording) {
            this.recorder.popMatrix();
        }
        strokeWeight(1);
        stroke(1);
        synchronized (this.elementsTextStatic) {
            for (Text t : this.elementsTextStatic) {
                if (this.zoomer.getZoomScale() < t.minScale) {
                    continue;
                }
                drawText(t);
            }
        }
        for (Text t : this.elementsText) {
            if (this.zoomer.getZoomScale() < t.minScale) {
                continue;
            }
            drawText(t);
        }

        synchronized (this.additionalDrawers) {
            for (VisDebuggerAdditionalDrawer d : this.additionalDrawers) {
                if (d instanceof VisDebuggerOverlay) {
                    d.drawText(this);
                }
            }
        }

        if (recording) {
            endRecord();
            this.zoomer = old;
            this.keyControl.awaitScreenshot();
            this.keyControl.informScreenshotPerformed();
        }

        if (this.fs != null) {
            this.fs.saveFrame(this);
        }

    }


    private void drawText(Text t) {
        float ts = (float) (18 * this.zoomer.getZoomScale() / t.minScale);
        textSize(ts);
        PVector cv = this.zoomer.getCoordToDisp(new PVector(t.x, t.y));
        fill(t.r, t.g, t.b, t.a);
        float w = textWidth(t.text);
        if (t.theta != 0) {
            pushMatrix();
            translate(cv.x, cv.y);
            rotate(t.theta);
            textAlign(CENTER);
            text(t.text, 0, 0);
            popMatrix();
        } else {
            textAlign(LEFT);
            text(t.text, cv.x - w / 2, cv.y + ts / 2);
        }
        // System.out.println(cv.x + "  " + cv.y);
    }

    private void drawTriangle(Triangle t) {
        if (this.zoomer.getZoomScale() < t.minScale) {
            return;
        }
        stroke(t.r, t.g, t.b, t.a);
        if (t.fill) {
            fill(t.r, t.g, t.b, t.a);
        } else {
            fill(0, 0);
        }

        triangle(t.x0, t.y0, t.x1, t.y1, t.x2, t.y2);
    }

    private void drawCircle(Circle c) {
        if (this.zoomer.getZoomScale() < c.minScale) {
            return;
        }
        if (c.fill) {
            //			fill(c.r, c.g, c.b, c.a);
            fill(c.r, c.g, c.b, 255);
        } else {
            fill(255, 0);
        }
        float r = c.rr;
        // stroke(c.r,c.g,c.b,c.a);
        // stroke(0,0,0,128);;
        if (this.zoomer.getZoomScale() < 25) {
            float incr = (float) (1 / this.zoomer.getZoomScale());
            r += incr;
        }
        stroke(0, (float) (255 * this.zoomer.getZoomScale() / 100) + 32);
        ellipseMode(RADIUS);
        ellipse(c.x, c.y, r, r);
        // filter(BLUR, 4);
    }

    private void drawPolygon(Polygon p) {
        // if (this.scale < p.minScale) {
        // return;
        // }
        if (this.zoomer.getZoomScale() < p.minScale) {
            return;
        }

        fill(p.r, p.g, p.b, p.a);
        // stroke(p.r,p.g,p.b,p.a);
        // strokeWeight(.5f);
        stroke(0);
        // stroke(0,0);
        beginShape();
        for (int i = 0; i < p.x.length; i++) {
            vertex(p.x[i], p.y[i]);
        }
        endShape();

    }

    private void drawLine(Line l) {
        if (this.zoomer.getZoomScale() < l.minScale) {
            return;
        }
        // if (this.scale < (l.minScale-l.a)) {
        // return;
        // }
        int a = l.a;
        // if (this.scale < l.minScale) {
        // a -= (int) (l.minScale-this.scale);
        // }
        stroke(l.r, l.g, l.b, a);
        // stroke(20);
        // strokeWeight(2);

        line(l.x0, l.y0, l.x1, l.y1);

    }

    private void drawRect(Rect r) {
        if (this.zoomer.getZoomScale() < r.minScale) {
            return;
        }


        stroke(0, 255);
        strokeWeight(0.001f);
        if (r.fill) {
            fill(r.r, r.g, r.b, r.a);
        } else {
            fill(0, 0);
        }
        rect(r.tx, r.ty, r.sx, r.sy);
    }

    /* package */void addTriangle(double x0, double y0, double x1, double y1,
                                  double x2, double y2, int r, int g, int b, int a, int minScale,
                                  boolean fill) {
        Triangle t = new Triangle();
        t.x0 = (float) (x0);
        t.x1 = (float) (x1);
        t.x2 = (float) (x2);

        t.y0 = (float) -(y0);
        t.y1 = (float) -(y1);
        t.y2 = (float) -(y2);

        t.r = r;
        t.g = g;
        t.b = b;
        t.a = a;
        t.minScale = minScale;
        t.fill = fill;
        addElement(t);

    }

    private void addElement(Object o) {
        if (o instanceof Text) {
            this.newElementsText.add((Text) o);
        } else {
            this.newElements.add(o);
        }
    }

    /* package */
    public void addCircle(double x, double y, float rr, int r, int g,
                          int b, int a, int minScale, boolean fill) {
        Circle c = new Circle();
        c.x = (float) (x);
        c.y = (float) -(y);
        c.rr = rr;
        c.r = r;
        c.g = g;
        c.b = b;
        c.a = a;
        c.minScale = minScale;
        c.fill = fill;
        addElement(c);
    }

    /* package */void addCircleStatic(double x, double y, float rr, int r,
                                      int g, int b, int a, int minScale) {
        Circle c = new Circle();
        c.x = (float) (x);
        c.y = (float) -(y);
        c.rr = rr;
        c.r = r;
        c.g = g;
        c.b = b;
        c.a = a;
        c.minScale = minScale;
        addElementStatic(c);
    }

    private void addElementStatic(Object o) {
        synchronized (this.elementsStatic) {
            if (o instanceof Text) {
                this.elementsTextStatic.add((Text) o);
            } else {
                this.elementsStatic.add(o);
            }
        }
    }

    /* package */void addTextStatic(double x, double y, String string,
                                    int minScale) {
        Text text = new Text();
        text.x = (float) (x);
        text.y = (float) -(y);
        text.text = string;
        text.a = 255;
        text.minScale = minScale;
        addElementStatic(text);
    }

    /* package */void addText(double x, double y, String string, int minScale) {
        Text text = new Text();
        text.x = (float) (x);
        text.y = (float) -(y);
        text.text = string;
        text.a = 255;
        text.minScale = minScale;
        addElement(text);
    }

    public void addText(double x, double y, String string, int minScale,
                        float atan) {
        Text text = new Text();
        text.x = (float) (x);
        text.y = (float) -(y);
        text.text = string;
        text.a = 255;
        text.minScale = minScale;
        text.theta = atan;
        addElement(text);

    }

    public void addRect(double tx, double ty, double sx, double sy, int r,
                        int g, int b, int a, int minScale, boolean fill) {
        Rect rect = new Rect();
        rect.tx = (float) (tx);
        rect.ty = (float) -(ty);
        rect.sx = (float) sx;
        rect.sy = (float) sy;
        rect.a = a;
        rect.r = r;
        rect.g = g;
        rect.b = b;
        rect.minScale = minScale;
        rect.fill = fill;

        addElement(rect);

    }

    public void addRectStatic(double tx, double ty, double sx, double sy, int r,
                              int g, int b, int a, int minScale, boolean fill) {
        Rect rect = new Rect();
        rect.tx = (float) (tx);
        rect.ty = (float) -(ty);
        rect.sx = (float) sx;
        rect.sy = (float) sy;
        rect.a = a;
        rect.r = r;
        rect.g = g;
        rect.b = b;
        rect.minScale = minScale;
        rect.fill = fill;

        addElementStatic(rect);

    }

    public void addPolygonStatic(double[] x, double[] y, int r, int g, int b,
                                 int a, int minScale) {
        Polygon p = new Polygon();
        float[] fx = new float[x.length];
        float[] fy = new float[x.length];
        for (int i = 0; i < x.length; i++) {
            fx[i] = (float) (x[i]);
            fy[i] = (float) -(y[i]);
        }

        p.x = fx;
        p.y = fy;
        p.r = r;
        p.g = g;
        p.b = b;
        p.a = a;
        p.minScale = minScale;
        addElementStatic(p);
    }

    public void addPolygon(List<Double> x, List<Double> y, int r, int g, int b,
                           int a, int minScale) {
        Polygon p = new Polygon();
        float[] fx = new float[x.size()];
        float[] fy = new float[x.size()];
        for (int i = 0; i < x.size(); i++) {
            fx[i] = (float) (+x.get(i));
            fy[i] = (float) (-y.get(i));
        }

        p.x = fx;
        p.y = fy;
        p.r = r;
        p.g = g;
        p.b = b;
        p.a = a;
        p.minScale = minScale;
        addElement(p);
    }

    public void update(double time) {
        if (this.fs != null && this.fs.incrSkipped()) {
            this.fs.await();
        }
        this.time2 = time;
        synchronized (this.elements) {
            // this.elements.clear();
            this.elements = Collections.synchronizedList(new ArrayList<Object>(
                    this.newElements));

            this.newElements.clear();
        }
        synchronized (this.elementsText) {
            this.elementsText = Collections
                    .synchronizedList(new ArrayList<Text>(this.newElementsText));
            this.newElementsText.clear();
        }
    }

    public void addDashedLineStatic(double x, double y, double x2, double y2,
                                    int r, int g, int b, int a, int minScale, double dash, double gap) {
        double dx = x2 - x;
        double dy = y2 - y;
        double l = Math.sqrt(dx * dx + dy * dy);
        dx /= l;
        dy /= l;
        double tl = 0;

        double tx = x;
        double ty = y;
        while (tl < l) {
            if (tl + dash > l) {
                addLineStatic(tx, ty, x2, y2, r, g, b, a, minScale);
            } else {
                addLineStatic(tx, ty, tx + dx * dash, ty + dy * dash, r, g, b,
                        a, minScale);
            }
            tx += dx * (dash + gap);
            ty += dy * (dash + gap);
            tl += dash + gap;
        }

        // TODO Auto-generated method stub

    }

    public void addLineStatic(double x0, double y0, double x1, double y1,
                              int r, int g, int b, int a, int minScale) {
        Line l = new Line();
        l.x0 = (float) (x0);
        l.x1 = (float) (x1);
        l.y0 = (float) -(y0);
        l.y1 = (float) -(y1);
        l.r = r;
        l.g = g;
        l.b = b;
        l.a = a;
        l.minScale = minScale;
        addElementStatic(l);

    }

    public void addDashedLine(double x, double y, double x2, double y2, int r,
                              int g, int b, int a, int minScale, double dash, double gap) {
        double dx = x2 - x;
        double dy = y2 - y;
        double l = Math.sqrt(dx * dx + dy * dy);
        dx /= l;
        dy /= l;
        double tl = 0;

        double tx = x;
        double ty = y;
        while (tl < l) {
            if (tl + dash > l) {
                addLine(tx, ty, x2, y2, r, g, b, a, minScale);
            } else {
                addLine(tx, ty, tx + dx * dash, ty + dy * dash, r, g, b, a,
                        minScale);
            }
            tx += dx * (dash + gap);
            ty += dy * (dash + gap);
            tl += dash + gap;
        }

        // TODO Auto-generated method stub

    }

    /* package */void addLine(double x0, double y0, double x1, double y1,
                              int r, int g, int b, int a, int minScale) {
        Line l = new Line();
        l.x0 = (float) (x0);
        l.x1 = (float) (x1);
        l.y0 = (float) -(y0);
        l.y1 = (float) -(y1);
        l.r = r;
        l.g = g;
        l.b = b;
        l.a = a;
        l.minScale = minScale;
        addElement(l);

    }

    void addAdditionalDrawer(VisDebuggerAdditionalDrawer drawer) {
        synchronized (this.additionalDrawers) {
            this.additionalDrawers.add(drawer);
        }
    }

    public void addKeyControl(Control keyControl) {
        this.addKeyListener(keyControl);
        this.addMouseWheelListener(keyControl);
        this.keyControl = keyControl;
    }

    public void reset(int it) {
        if (it < 10) {
            this.it = "it.00" + it + "_";
        } else {
            if (it < 100) {
                this.it = "it.0" + it + "_";
            } else {
                this.it = "it." + it + "_";
            }
        }

    }

    private static class Triangle {
        boolean fill = true;
        float x0, x1, x2, y0, y1, y2;
        int r, g, b, a, minScale;
    }

    private static class Circle {
        boolean fill = true;
        float x, y, rr;
        int r, g, b, a, minScale = 0;
    }

    private static class Polygon {
        float[] x;
        float[] y;
        int r, g, b, a, minScale = 0;
    }

    private static final class Line {
        float x0, x1, y0, y1;
        int r, g, b, a, minScale = 0;
    }

    private static final class Rect {
        public boolean fill;
        float tx, ty, sx, sy;
        int r, g, b, a, minScale = 0;
    }

    static final class Text {
        float x, y, theta = 0;
        String text = "";
        int r = 0, g = 0, b = 0, a = 255;
        int minScale = 0;
    }
}

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

import org.apache.log4j.Logger;
import org.gicentre.utils.move.ZoomPan;
import processing.core.PVector;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.*;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Control implements KeyListener, MouseWheelListener {

    private static final Logger log = Logger.getLogger(Control.class);
    // zoom:1389.2476:778.86145x2.0789281794113688 at key 0
    // 2683.7285:1353.723x3.7334563223415764
    //zoom: 22.704667199218342 at: 11619.073,8703.7
    private static final Zoom z = new Zoom();

    static {

        z.z = 39.96581922606923;
        z.o = new PVector(18529.309f, 14728.958f);
    }

    private final CyclicBarrier pauseBarrier = new CyclicBarrier(2);
    private final CyclicBarrier screenshotBarrier = new CyclicBarrier(2);
    private final ZoomPan zoomer;
    private final FrameSaver fs;
    private final int cameraTrackFrameSkip;
    private double speedup = 1;
    private boolean pause = false;
    private Map<Integer, Zoom> zooms = new HashMap<Integer, Zoom>();
    private boolean makeScreenshot = false;
    private boolean recordCameraMovement = false;
    private CameraMovement current = new CameraMovement();

    // static zoom
    // 13189.192:7476.9673x20.593802448270605
    // zoom:96268.414:56770.85x152.2291436143277 at key 0
    private boolean replayCameraMovement;
    private Map<Integer, CameraMovement> movements = new HashMap<Integer, CameraMovement>();

    public Control(ZoomPan zoomer, int cameraTrackFrameSkip, FrameSaver fs) {
        this.zoomer = zoomer;
        this.zoomer.setZoomScale(z.z);
        this.zoomer.setPanOffset(z.o.x, z.o.y);
        int c = '0';
        this.zooms.put(c, z);
        this.current.cameraTrackFrameSkip = cameraTrackFrameSkip;
        this.current.cameraTrackFrameSkipDbl = cameraTrackFrameSkip;
        this.movements.put(0, this.current);
        this.cameraTrackFrameSkip = cameraTrackFrameSkip;
        this.fs = fs;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == '+') {
            if (this.speedup >= 512) {
                log.info("acceleration already at maximum (512).");
                return;
            }
            this.speedup *= 2;
            // System.out.println("speedup:" + this.speedup);
            log.info("setting accelaration to:" + this.speedup);
        } else if (e.getKeyChar() == '-') {
            if (this.speedup <= 0.125) {
                log.info("acceleration already at minimum (0.125).");
                return;
            }
            this.speedup /= 2;
            log.info("setting accelaration to:" + this.speedup);
        } else if (e.getKeyChar() == 'p') {
            log.info("toggle pause");
            if (this.pause) {
                awaitPause();
            }
            this.pause = !this.pause;
        } else if (e.getKeyChar() == 's') {
            log.info("screenshot requested");
            this.makeScreenshot = true;
        } else if (e.getKeyChar() == 'i') {
            Zoom z = new Zoom();
            z.o = this.zoomer.getPanOffset();
            z.z = this.zoomer.getZoomScale();
            log.info(z);
        } else if (e.getKeyChar() == 'r') {
            log.info("toggle recording camera movement");
            if (this.recordCameraMovement) {
                this.current.stepSize = (double) this.current.cameraTrackFrameSkip
                        / this.current.movement.size();

            }
            this.recordCameraMovement = !this.recordCameraMovement;
        } else if (e.getKeyChar() == 'e') {
            if (this.recordCameraMovement) {
                throw new RuntimeException("stop recording first!");
            }
            this.replayCameraMovement = true;
            this.current.replayCameraMovementIT = this.current.movement
                    .iterator();
        } else if (e.getKeyChar() == 'w') {
            if (this.recordCameraMovement) {
                throw new RuntimeException("stop recording first!");
            }
            this.current.stepSize *= -1;
            this.replayCameraMovement = true;
            Collections.reverse(this.current.movement);
            this.current.replayCameraMovementIT = this.current.movement
                    .iterator();
        } else if (e.getKeyChar() == 'o') {
            log.info("serializing zooms and camera movements");
            try {
                FileOutputStream out = new FileOutputStream(
                        "/Users/laemmel/devel/hhw_hybrid/zooms.data");
                ObjectOutputStream oOut = new ObjectOutputStream(out);
                oOut.writeObject(this.zooms);
                oOut.writeObject(this.movements);
                oOut.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        } else if (e.getKeyChar() == 'l') {
            log.info("restoring zooms and camera movements");
            try {
                FileInputStream out = new FileInputStream(
                        "/Users/laemmel/devel/hhw_hybrid/zooms.data");
                ObjectInputStream oOut = new ObjectInputStream(out);
                // oOut.writeObject(this.zooms);
                Object o1 = oOut.readObject();
                this.zooms = (Map<Integer, Zoom>) o1;
                Object o2 = oOut.readObject();
                this.movements = (Map<Integer, CameraMovement>) o2;
                for (CameraMovement m : this.movements.values()) {
                    m.cameraTrackFrameSkip = this.cameraTrackFrameSkip;
                    m.cameraTrackFrameSkipDbl = this.cameraTrackFrameSkip;
                    m.stepSize = (double) m.cameraTrackFrameSkip
                            / (m.movement.size() - 1);
                }
                Zoom z = this.zooms.get(0);
                if (z != null) {
                    log.info("loading zoom:" + z.o.x + ":" + z.o.y + "x" + z.z);
                    this.zoomer.setZoomScale(z.z);
                    this.zoomer.setPanOffset(z.o.x, z.o.y);
                    // this.zoomer.transform();
                }
                this.current = this.movements.get(0);
                oOut.close();
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }

        // this works with Mac
        if (e.getModifiers() == 4 && e.getKeyCode() >= 48
                && e.getKeyCode() <= 57) {
            Zoom z = new Zoom();
            z.z = this.zoomer.getZoomScale();
            z.o = this.zoomer.getPanOffset();
            this.zooms.put(e.getKeyCode(), z);
            log.info("storing zoom:" + z.o.x + ":" + z.o.y + "x" + z.z
                    + " at key " + e.getKeyChar());
        } else if (e.getModifiers() == 2 && e.getKeyCode() >= 48
                && e.getKeyCode() <= 57) {
            log.info("selecting record: " + (e.getKeyCode() - 48));
            this.current = this.movements.get(e.getKeyCode() - 48);
            if (this.current == null) {
                log.info("record does not exist, creating new one!");
                this.current = new CameraMovement();
                this.current.cameraTrackFrameSkip = this.cameraTrackFrameSkip;
                this.current.cameraTrackFrameSkipDbl = this.cameraTrackFrameSkip;
                this.movements.put(e.getKeyCode() - 48, this.current);
            }

        } else if (e.getKeyCode() >= 48 && e.getKeyCode() <= 57) {
            Zoom z = this.zooms.get(e.getKeyCode());
            if (z != null) {
                log.info("loading zoom:" + z.o.x + ":" + z.o.y + "x" + z.z);
                this.zoomer.setZoomScale(z.z);
                this.zoomer.setPanOffset(z.o.x, z.o.y);
                // this.zoomer.transform();
            }
        }
        System.out.println(e.getKeyCode() + "  " + e.getModifiers());

    }

    public void awaitPause() {
        if (!this.pause) {
            return;
        }
        try {
            this.pauseBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

    }

    public void awaitScreenshot() {
        if (!this.makeScreenshot) {
            return;
        }
        try {
            this.screenshotBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    public boolean isScreenshotRequested() {
        return this.makeScreenshot;
    }

    public void informScreenshotPerformed() {
        this.makeScreenshot = false;
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub

    }

    public double getSpeedup() {
        return this.speedup;
    }

    public boolean isOneObjectWaitingAtScreenshotBarrier() {
        return this.screenshotBarrier.getNumberWaiting() == 1;
    }

    public void requestScreenshot() {
        this.makeScreenshot = true;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent arg0) {
        if (this.recordCameraMovement) {
            Zoom z = new Zoom();
            z.o = this.zoomer.getPanOffset();
            z.z = this.zoomer.getZoomScale();
            this.current.movement.add(z);
            log.info(z);
        }


    }

    public void update(double time2) {
        if (this.replayCameraMovement
                && (this.current.skip++) >= this.current.cameraTrackFrameSkipDbl) {
            if (this.current.replayCameraMovementIT.hasNext()) {
                Zoom z = this.current.replayCameraMovementIT.next();
                log.info("loading zoom:" + z.o.x + ":" + z.o.y + "x" + z.z
                        + " skips =" + this.current.cameraTrackFrameSkipDbl);
                this.zoomer.setZoomScale(z.z);
                this.zoomer.setPanOffset(z.o.x, z.o.y);
                this.current.cameraTrackFrameSkipDbl -= this.current.stepSize;
            } else {

                this.replayCameraMovement = false;
                double d1 = Math.abs(this.current.cameraTrackFrameSkipDbl);
                double d2 = Math.abs(this.current.cameraTrackFrameSkipDbl
                        - this.current.cameraTrackFrameSkip);
                if (d1 < d2) {
                    this.current.cameraTrackFrameSkipDbl = 0;
                } else {
                    this.current.cameraTrackFrameSkipDbl = this.current.cameraTrackFrameSkip;
                }
                if (this.current.movement.get(0).z > this.current.movement
                        .get(this.current.movement.size() - 1).z) {
                    Zoom z = this.zooms.get(48);
                    log.info("loading zoom:" + z.o.x + ":" + z.o.y + "x" + z.z
                            + " skips =" + this.current.cameraTrackFrameSkipDbl);
                    this.zoomer.setZoomScale(z.z);
                    this.zoomer.setPanOffset(z.o.x, z.o.y);
                }
            }
            // this.zoomer.
            this.current.skip = 0;
            if (this.fs != null) {
                int round = (int) (this.current.cameraTrackFrameSkipDbl + .5);
                this.fs.setSkip(round);
            }
        }

    }

    private static final class Zoom implements Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 5008396314401427431L;
        public PVector o;
        public double z;

        @Override
        public String toString() {
            return "zoom: " + this.z + " at: " + this.o.x + "," + this.o.y;
        }

    }

    private static final class CameraMovement implements Serializable {
        /**
         *
         */
        private static final long serialVersionUID = 4206049019877073858L;

        double stepSize;
        List<Zoom> movement = new ArrayList<Zoom>();
        Iterator<Zoom> replayCameraMovementIT;
        int cameraTrackFrameSkip;
        int skip = 0;
        double cameraTrackFrameSkipDbl;
    }

}

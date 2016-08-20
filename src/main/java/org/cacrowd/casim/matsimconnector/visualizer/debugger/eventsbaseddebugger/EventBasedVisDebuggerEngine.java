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

package org.cacrowd.casim.matsimconnector.visualizer.debugger.eventsbaseddebugger;

import org.cacrowd.casim.environment.TransitionArea;
import org.cacrowd.casim.matsimconnector.agents.Pedestrian;
import org.cacrowd.casim.matsimconnector.events.*;
import org.cacrowd.casim.matsimconnector.events.debug.*;
import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.matsimconnector.scenario.CAScenario;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.utility.MathUtility;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.PedestrianGrid;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.pedca.utility.FileUtility;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;
import java.util.*;

public class EventBasedVisDebuggerEngine implements CAEventHandler, LineEventHandler, ForceReDrawEventHandler, RectEventHandler {

    //    private final CAScenario scenarioCA;
    private final Scenario sc;
    private final Collection<CAEnvironment> caEnvs;
    @SuppressWarnings("rawtypes")
    private final Map<Id, CircleProperty> circleProperties = new HashMap<Id, CircleProperty>();
    private final CircleProperty defaultCp = new CircleProperty();
    private final double dT;
    private final List<ClockedVisDebuggerAdditionalDrawer> drawers = new ArrayList<ClockedVisDebuggerAdditionalDrawer>();


    //	private final Scenario sc;
    double time;
    FrameSaver fs = null;
    boolean environmentInit = false;
    private EventsBasedVisDebugger vis;
    private long lastUpdate = -1;
    private Control keyControl;
    private int nrAgents;

    public EventBasedVisDebuggerEngine(Collection<CAEnvironment> caEnvs) {
        this.sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
//		this.scenarioCA = caScenario;
        this.caEnvs = caEnvs;
        this.dT = Constants.CA_STEP_DURATION;
        this.vis = new EventsBasedVisDebugger(this.fs);

        this.keyControl = new Control(this.vis.zoomer, 90, this.fs);
        this.vis.addKeyControl(this.keyControl);
        if (!environmentInit) {
            drawNodesAndLinks();
            drawCAEnvironments();
            environmentInit = true;
        }
    }

    public EventBasedVisDebuggerEngine(Scenario sc) {
        this.sc = sc;
        this.caEnvs = ((CAScenario) sc.getScenarioElement(Constants.CASCENARIO_NAME)).getEnvironments().values();
        this.dT = Constants.CA_STEP_DURATION;
        this.vis = new EventsBasedVisDebugger(sc, this.fs);

        this.keyControl = new Control(this.vis.zoomer, 90, this.fs);
        this.vis.addKeyControl(this.keyControl);
    }


    public void startIteration(int iteration) {
        fs = null;
        if ((iteration % 2 == 0) && Constants.SAVE_FRAMES) {
            String pathName = Constants.PATH + "/videos/frames/it" + iteration;
            FileUtility.deleteDirectory(new File(pathName));
            fs = new FrameSaver(pathName, "png", 30);
        }
        this.vis.fs = fs;
        this.keyControl.fs = fs;
    }

    public void addAdditionalDrawer(VisDebuggerAdditionalDrawer drawer) {
        this.vis.addAdditionalDrawer(drawer);
        if (drawer instanceof ClockedVisDebuggerAdditionalDrawer) {
            this.drawers.add((ClockedVisDebuggerAdditionalDrawer) drawer);
        }
    }

    private void drawNodesAndLinks() {
        for (Node n : sc.getNetwork().getNodes().values()) {
            this.vis.addCircleStatic(n.getCoord().getX(), n.getCoord().getY(), .2f, 0, 0, 0, 255, 0);
            this.vis.addTextStatic(n.getCoord().getX(), n.getCoord().getY()-.3, ""+ n.getId().toString(), 10);
        }
        for (Link l : sc.getNetwork().getLinks().values()) {

            Node from = l.getFromNode();
            Node to = l.getToNode();
            
            double textXdiff = to.getCoord().getX() - from.getCoord().getX();
            double textYdiff = to.getCoord().getY() - from.getCoord().getY();
            
            double textX = (from.getCoord().getX()+to.getCoord().getX())/2 + textXdiff/3;
            double textY = (from.getCoord().getY()+to.getCoord().getY())/2 + textYdiff/3;
            
            this.vis.addTextStatic(textX, textY, ""+ l.getId().toString(), 150);

            if (from != null && to != null) {
                boolean isStairs = false;
                for (String stairId : Constants.stairsLinks) {
                    if (stairId.equals(l.getId().toString()))
                        isStairs = true;
                }
                if (isStairs)
                    this.vis.addLineStatic(from.getCoord().getX(), from.getCoord().getY(), to.getCoord().getX(),
                            to.getCoord().getY(), 255, 255, 0, 255, 0);
                else
                    this.vis.addLineStatic(from.getCoord().getX(), from.getCoord().getY(), to.getCoord().getX(),
                            to.getCoord().getY(), 0, 0, 0, 255, 0);
            }
        }
    }

    private void drawCAEnvironments() {
        caEnvs.forEach(this::drawCAEnvironment);
    }

    public void drawCAEnvironment(CAEnvironment environmentCA) {
        Coordinate origin = MathUtility.sum(environmentCA.getContext().environmentOrigin,new Coordinate(10+environmentCA.getContext().getColumns()*Constants.CA_CELL_SIDE,0));
		drawObjects(environmentCA.getContext().getEnvironmentGrid(), origin);
        for (PedestrianGrid pedestrianGrid : environmentCA.getContext().getPedestrianGrids())
            drawPedestrianGridBorders(pedestrianGrid, origin);
    }

    private void drawObjects(EnvironmentGrid environmentGrid, Coordinate origin) {
        for (int y = 0; y < environmentGrid.getRows(); y++)
            for (int x = 0; x < environmentGrid.getColumns(); x++)
                if (environmentGrid.getCellValue(y, x) == org.cacrowd.casim.pedca.utility.Constants.ENV_OBSTACLE)
                    drawObstacle(environmentGrid, new GridPoint(x, y), origin);
                else if (environmentGrid.belongsToTacticalDestination(new GridPoint(x, y)))
                    drawTacticalDestinationCell(environmentGrid, new GridPoint(x, y), origin);
    }

    private void drawTacticalDestinationCell(EnvironmentGrid grid, GridPoint gridPoint, Coordinate origin) {
        Coordinate bottomLeft = grid.gridPoint2Coordinate(gridPoint);
        bottomLeft.setX(bottomLeft.getX()+origin.getX());
        bottomLeft.setY(bottomLeft.getY()+origin.getY());
        this.vis.addRectStatic(bottomLeft.getX(), bottomLeft.getY() + Constants.CA_CELL_SIDE, Constants.CA_CELL_SIDE, Constants.CA_CELL_SIDE, 150, 150, 255, 150, 0, true);

    }

    private void drawObstacle(EnvironmentGrid grid, GridPoint gridPoint, Coordinate origin) {
        Coordinate bottomLeft = grid.gridPoint2Coordinate(gridPoint);
        bottomLeft.setX(bottomLeft.getX()+origin.getX());
        bottomLeft.setY(bottomLeft.getY()+origin.getY());
        this.vis.addRectStatic(bottomLeft.getX(), bottomLeft.getY() + Constants.CA_CELL_SIDE, Constants.CA_CELL_SIDE, Constants.CA_CELL_SIDE, 80, 80, 80, 192, 0, true);
    }

    private void drawPedestrianGridBorders(PedestrianGrid pedestrianGrid, Coordinate origin) {
        LineProperty lp = new LineProperty();
        lp.r = 0;
        lp.g = 0;
        lp.b = 0;
        lp.a = 192;

        int rows = pedestrianGrid.getRows();
        int columns = pedestrianGrid.getColumns();
        ArrayList<Coordinate> gridCoordinates = new ArrayList<Coordinate>();
        gridCoordinates.add(calculateCoordinates(0, 0, pedestrianGrid));
        gridCoordinates.add(calculateCoordinates(columns, 0, pedestrianGrid));
        gridCoordinates.add(calculateCoordinates(columns, rows, pedestrianGrid));
        gridCoordinates.add(calculateCoordinates(0, rows, pedestrianGrid));
        gridCoordinates.add(calculateCoordinates(0, 0, pedestrianGrid));

        Iterator<Coordinate> it = gridCoordinates.iterator();
        Coordinate c0;
        Coordinate c1 = it.next();

        while (it.hasNext()) {
            c0 = c1;
            c1 = it.next();
            if (pedestrianGrid instanceof TransitionArea)
                this.vis.addDashedLineStatic(c0.getX()+origin.getX(), c0.getY()+origin.getY(), c1.getX()+origin.getX(), c1.getY()+origin.getY(), 0, lp.g, lp.b, lp.a, 0, .3, 0.15);
            else
                this.vis.addLineStatic(c0.getX()+origin.getX(), c0.getY()+origin.getY(), c1.getX()+origin.getX(), c1.getY()+origin.getY(), lp.r, lp.g, lp.b, lp.a, 0);
        }
    }

    private Coordinate calculateCoordinates(int x, int y, PedestrianGrid pedestrianGrid) {
        Coordinate result;
        if (pedestrianGrid instanceof TransitionArea) {
            result = pedestrianGrid.gridPoint2Coordinate(new GridPoint(x, y));
            result = ((TransitionArea) pedestrianGrid).convertCoordinates(result);
        } else
            result = pedestrianGrid.gridPoint2Coordinate(new GridPoint(x, y));
        return result;
    }

    @Override
    public void reset(int iteration) {
        this.time = -1;
        //this.vis.reset(iteration);
    }

    public void handleEvent(CAAgentMoveEvent event) {
        if (event.getRealTime() >= this.time + Constants.CA_STEP_DURATION) {
            update(this.time);
            this.time = event.getRealTime();
        }

        this.nrAgents++;

        Coordinate origin = MathUtility.sum(event.getPedestrian().getContext().environmentOrigin,new Coordinate(10+event.getPedestrian().getContext().getColumns()*Constants.CA_CELL_SIDE,0));

        double from_x = MathUtility.convertGridCoordinate(event.getFrom_x())+origin.getX();
        double from_y = MathUtility.convertGridCoordinate(event.getFrom_y())+origin.getY();
        double to_x = MathUtility.convertGridCoordinate(event.getTo_x())+origin.getX();
        double to_y = MathUtility.convertGridCoordinate(event.getTo_y())+origin.getY();

        
		/*
        GridPoint deltaPos = DirectionUtility.convertHeadingToGridPoint(event.getDirection());
		double to_x_triangle = MathUtility.convertGridCoordinate(event.getFrom_x()+deltaPos.getX());
		double to_y_triangle = MathUtility.convertGridCoordinate(event.getFrom_y()+deltaPos.getY());
		double dx = (to_y_triangle - from_y);
		double dy = -(to_x_triangle - from_x);
		double length = Math.sqrt(dx*dx+dy*dy);
		dx /= length;
		dy /= length;
		
		double x0 = to_x_triangle;
		double y0 = to_y_triangle;
		double al = .20;
		double x1 = x0 + dy*al -dx*al/4;
		double y1 = y0 - dx*al -dy*al/4;
		double x2 = x0 + dy*al +dx*al/4;
		double y2 = y0 - dx*al +dy*al/4;
		*/

        double z = this.vis.zoomer.getZoomScale();
        int a = 255;
        if (z >= 48 && z < 80) {
            z -= 48;
            a = (int) (255. / 32 * z + .5);
        }
        this.vis.addLine(to_x, to_y, from_x, from_y, 0, 0, 0, a, 50);
        //this.vis.addTriangle(x0, y0, x1, y1, x2, y2, 0, 0, 0, a, 50, true);

        CircleProperty cp = this.circleProperties.get(event.getPedestrian().getId());
        if (cp == null) {
            cp = this.defaultCp;
        }

        this.vis.addCircle(to_x, to_y, cp.rr, cp.r, cp.g, cp.b, cp.a, cp.minScale, cp.fill);
        this.vis.addText(to_x, to_y, "" + event.getPedestrian().getTimeToCrossDest(), 200);
    }

    private void update(double time2) {
        this.keyControl.awaitPause();
        this.keyControl.awaitScreenshot();
        this.keyControl.update(time2);
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

        this.vis.update(this.time);
        this.lastUpdate = System.currentTimeMillis();
        for (ClockedVisDebuggerAdditionalDrawer drawer : this.drawers) {
            drawer.update(this.lastUpdate);
            if (drawer instanceof InfoBox) {
                ((InfoBox) drawer).setNrAgents(this.nrAgents);
            }
        }
        this.nrAgents = 0;
    }

    public void handleEvent(CAAgentConstructEvent event) {
        if (!environmentInit) {
            drawNodesAndLinks();
            drawCAEnvironments();
            environmentInit = true;
        }

        Pedestrian pedestrian = event.getPedestrian();
        CircleProperty cp = new CircleProperty();
        cp.rr = (float) (0.8 / 5.091);
        this.circleProperties.put(pedestrian.getId(), cp);
        updateColor(pedestrian);
       
    }

    @Override
    public void handleEvent(CAAgentExitEvent event) {
        this.circleProperties.remove(event.getPedestrian().getId());
    }

    @Override
    public void handleEvent(LineEvent e) {

        if (e.isStatic()) {
            if (e.getGap() == 0) {
                this.vis.addLineStatic(e.getSegment().x0, e.getSegment().y0, e.getSegment().x1, e.getSegment().y1, e.getR(), e.getG(), e.getB(), e.getA(), e.getMinScale());
            } else {
                this.vis.addDashedLineStatic(e.getSegment().x0, e.getSegment().y0, e.getSegment().x1, e.getSegment().y1, e.getR(), e.getG(), e.getB(), e.getA(), e.getMinScale(), e.getDash(), e.getGap());
            }
        } else {
            if (e.getGap() == 0) {
                this.vis.addLine(e.getSegment().x0, e.getSegment().y0, e.getSegment().x1, e.getSegment().y1, e.getR(), e.getG(), e.getB(), e.getA(), e.getMinScale());
            } else {
                this.vis.addDashedLine(e.getSegment().x0, e.getSegment().y0, e.getSegment().x1, e.getSegment().y1, e.getR(), e.getG(), e.getB(), e.getA(), e.getMinScale(), e.getDash(), e.getGap());
            }
        }

    }

    @Override
    public void handleEvent(ForceReDrawEvent event) {
        this.keyControl.requestScreenshot();
        update(event.getTime());
    }

    @Override
    public void handleEvent(RectEvent e) {
        this.vis.addRect(e.getTx(), e.getTy(), e.getSx(), e.getSy(), 255, 255, 255, 255, 0, e.getFill());
    }

    public int getNrAgents() {
        return this.nrAgents;
    }

    @Override
    public void handleEvent(CAAgentMoveToOrigin event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleEvent(CAAgentEnterEnvironmentEvent event) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleEvent(CAAgentChangeLinkEvent event) {
        //PedestrianProto pedestrian = event.getPedestrian();
        //updateColor(pedestrian);
    }

    private void updateColor(Pedestrian pedestrian) {
        CircleProperty cp = this.circleProperties.get(pedestrian.getId());
        String idDestination = pedestrian.getVehicle().getDriver().getDestinationLinkId().toString();
        //double xOrigin = pedestrian.getOriginMarker().getCoordinate().getX();
        //int color;
        //int origLevel = pedestrian.getOriginMarker().getLevel();
        //int color = (((destLevel+1)*origLevel)*100)%256;
        int brightness = 80;
        if (idDestination.endsWith("SG")) {	//Staten Island - St. George
            cp.r = 255;
            cp.g = brightness;
            cp.b = brightness;//255-color;
            cp.a = 255;
        } else if (idDestination.endsWith("WH")) { //Staten Island - St. George
            cp.r = brightness;
            cp.g = brightness;
            cp.b = 255;//255-color;
            cp.a = 255;
        }else {
            cp.r = 255;
            cp.g = 255;
            cp.b = 255;//255-color;
            cp.a = 255;
            System.out.println(idDestination);
        }
    }

    @Override
    public void handleEvent(CAEngineStepPerformedEvent event) {
        // TODO Auto-generated method stub

    }

    private static final class CircleProperty {
        boolean fill = true;
        float rr;
        int r, g, b, a, minScale = 0;
    }

    private static final class LineProperty {
        public int r, g, b, a = 0;
    }
}
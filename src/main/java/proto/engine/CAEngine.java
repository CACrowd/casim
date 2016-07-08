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

import com.google.inject.Inject;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import matsimconnector.scenario.CAEnvironment;
import matsimconnector.scenario.CAScenario;
import matsimconnector.utility.Constants;
import org.matsim.api.core.v01.Id;
import pedca.context.Context;
import pedca.engine.SimulationEngine;
import pedca.environment.grid.EnvironmentGrid;
import pedca.environment.grid.GridPoint;
import pedca.environment.markers.MarkerConfiguration;
import pedca.environment.network.Coordinate;
import pedca.output.CAScenarioWriter;
import proto.HybridSimProto;
import proto.scenario.ProtoCAScenario;

import java.io.IOException;
import java.util.*;

/**
 * Created by laemmel on 05/05/16.
 */
public class CAEngine {


    @Inject
    private ProtoCAScenario protoCAScenario;


    private Map<Id<CAEnvironment>, SimulationEngine> enginesCA = new HashMap<>();
    private double simCATime;

    public void doSimStep(double time) {
        double stepDuration = Constants.CA_STEP_DURATION;
        //Log.log("------> BEGINNING STEPS AT "+time);
        for (; this.simCATime < time; this.simCATime += stepDuration) {
            for (SimulationEngine engine : this.enginesCA.values()) {
                double currentTime = System.currentTimeMillis();
                engine.doSimStep(this.simCATime);
                double afterTime = System.currentTimeMillis();
//				qSim.getEventsManager().processEvent(new CAEngineStepPerformedEvent(this.simCATime, (float)(afterTime-currentTime), engine.getAgentGenerator().getContext().getPopulation().size()));
            }
        }
    }

    public void prepareSim(HybridSimProto.Scenario request) {
        Envelope envelope = new Envelope();
        for (HybridSimProto.Room r : request.getEnvironment().getRoomList()) {
            for (HybridSimProto.Subroom sub : r.getSubroomList()) {
                for (HybridSimProto.Polygon p : sub.getPolygonList()) {
                    for (HybridSimProto.Coordinate c : p.getCoordinateList()) {
                        envelope.expandToInclude(c.getX(), c.getY());
                    }
                }
            }
        }
        System.out.println(envelope);
        int rows = (int) (envelope.getHeight() / Constants.CA_CELL_SIDE) + 1;
        int cols = (int) (envelope.getWidth() / Constants.CA_CELL_SIDE) + 1;
        EnvironmentGrid grid = new EnvironmentGrid(rows, cols, envelope.getMinX(), envelope.getMinY());
        Quadtree quadtree = new Quadtree();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid.setCellValue(i, j, -1);
                GridPoint gp = new GridPoint(i, j);
                Coordinate c = grid.gridPoint2Coordinate(gp);
                Envelope e = new Envelope(c.getX(), c.getX() + Constants.CA_CELL_SIDE, c.getY(), c.getY() + Constants.CA_CELL_SIDE);
                quadtree.insert(e, gp);
            }
        }
        GeometryFactory gF = new GeometryFactory();
        for (HybridSimProto.Room r : request.getEnvironment().getRoomList()) {
            for (HybridSimProto.Subroom sub : r.getSubroomList()) {
                List<com.vividsolutions.jts.geom.Coordinate> coords = new ArrayList<>();

                for (HybridSimProto.Polygon p : sub.getPolygonList()) {
                    for (HybridSimProto.Coordinate c : p.getCoordinateList()) {
                        coords.add(new com.vividsolutions.jts.geom.Coordinate(c.getX(), c.getY()));

                    }


                }
                com.vividsolutions.jts.geom.Coordinate[] coordsA = coords.toArray(new com.vividsolutions.jts.geom.Coordinate[0]);

                Polygon poly = (Polygon) gF.createMultiPoint(coordsA).convexHull();

                Envelope e = poly.getEnvelopeInternal();
                List<GridPoint> gps = quadtree.query(e);
                for (GridPoint p : gps) {
                    Coordinate c = grid.gridPoint2Coordinate(p);
                    com.vividsolutions.jts.geom.Coordinate[] cc = {new com.vividsolutions.jts.geom.Coordinate(c.getX(), c.getY()),
                            new com.vividsolutions.jts.geom.Coordinate(c.getX(), c.getY() + Constants.CA_CELL_SIDE),
                            new com.vividsolutions.jts.geom.Coordinate(c.getX() + Constants.CA_CELL_SIDE, c.getY() + Constants.CA_CELL_SIDE),
                            new com.vividsolutions.jts.geom.Coordinate(c.getX() + Constants.CA_CELL_SIDE, c.getY()),
                            new com.vividsolutions.jts.geom.Coordinate(c.getX(), c.getY())};
                    Polygon cPoly = gF.createPolygon(cc);
                    if (poly.contains(cPoly)) { //walkable
                        grid.setCellValue(p.getY(), p.getX(), 0);
                    }

                }
            }
        }

        for (HybridSimProto.Transition transition : request.getEnvironment().getTransitionList()) {
            LineString ls = gF.createLineString(new com.vividsolutions.jts.geom.Coordinate[]{new com.vividsolutions.jts.geom.Coordinate(
                    transition.getVert1().getX(), transition.getVert1().getY()), new com.vividsolutions.jts.geom.Coordinate(
                    transition.getVert2().getX(), transition.getVert2().getY())});
            Envelope te = ls.getEnvelopeInternal();
            List<GridPoint> gps = quadtree.query(te);
            for (GridPoint p : gps) {
                Coordinate c = grid.gridPoint2Coordinate(p);
                com.vividsolutions.jts.geom.Coordinate[] cc = {new com.vividsolutions.jts.geom.Coordinate(c.getX(), c.getY()),
                        new com.vividsolutions.jts.geom.Coordinate(c.getX(), c.getY() + Constants.CA_CELL_SIDE),
                        new com.vividsolutions.jts.geom.Coordinate(c.getX() + Constants.CA_CELL_SIDE, c.getY() + Constants.CA_CELL_SIDE),
                        new com.vividsolutions.jts.geom.Coordinate(c.getX() + Constants.CA_CELL_SIDE, c.getY()),
                        new com.vividsolutions.jts.geom.Coordinate(c.getX(), c.getY())};
                Polygon cPoly = gF.createPolygon(cc);
                if (ls.intersects(cPoly)) {
                    grid.setCellValue(p.getY(), p.getX(), -2);
                }

            }
        }

        Context context = new Context(grid, new MarkerConfiguration());
        CAEnvironment env = new CAEnvironment("1", context);
        this.protoCAScenario.addCAEnvironment(env);

        try {
            new CAScenarioWriter(grid).write("src/main/js/grid.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            grid.saveCSV("/Users/laemmel/tmp/");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("done");
    }

}

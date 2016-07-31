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
import matsimconnector.scenario.CAEnvironment;
import matsimconnector.utility.Constants;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import pedca.context.Context;
import pedca.engine.SimulationEngine;
import pedca.environment.grid.EnvironmentGrid;
import pedca.environment.markers.MarkerConfiguration;
import pedca.output.CAScenarioWriter;
import proto.HybridSimProto;
import proto.geom.Edge;
import proto.geom.Rasterizer;
import proto.scenario.ProtoCAScenario;

import java.io.IOException;
import java.util.*;

/**
 * Created by laemmel on 05/05/16.
 */
public class CAEngine {

    private static final Logger log = Logger.getLogger(CAEngine.class);


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

        log.debug(envelope);
        int rows = (int) (envelope.getHeight() / Constants.CA_CELL_SIDE) + 2;
        int cols = (int) (envelope.getWidth() / Constants.CA_CELL_SIDE) + 2;

        EnvironmentGrid grid = new EnvironmentGrid(rows, cols, envelope.getMinX(), envelope.getMinY());
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid.setCellValue(i, j, -1);
            }
        }

        Rasterizer r = new Rasterizer(grid);
        r.rasterize(createEdgeTable(request.getEnvironment(), grid));


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


    private List<Edge> createEdgeTable(HybridSimProto.Environment environment, EnvironmentGrid grid) {
        List<Edge> edgeTable = new ArrayList<>();
        for (HybridSimProto.Room r : environment.getRoomList()) {
            for (HybridSimProto.Subroom s : r.getSubroomList()) {

                for (HybridSimProto.Polygon p : s.getPolygonList()) {
                    for (int i = 1; i < p.getCoordinateList().size(); i++) {
                        HybridSimProto.Coordinate c1 = p.getCoordinate(i - 1);
                        HybridSimProto.Coordinate c2 = p.getCoordinate(i);
//                        int row1 = grid.y2Row(c1.getY());
//                        int row2 = grid.y2Row(c2.getY());
//                        if (row1 == row2) {
//                            continue; //ignore horizontal segments
//                        }
                        Edge e = new Edge(c1.getX(), c1.getY(), c2.getX(), c2.getY(), Rasterizer.EdgeType.WALL);
                        edgeTable.add(e);


                    }
                }
            }
        }
        log.debug("Extracted: " + edgeTable.size() + " wall edges");
        for (HybridSimProto.Transition tr : environment.getTransitionList()) {
            Edge edge;
            if (tr.getSubroom2Id() != -1) {
                edge = new Edge(tr.getVert1().getX(), tr.getVert1().getY(), tr.getVert2().getX(), tr.getVert2().getY(), Rasterizer.EdgeType.TRANSITION_INTERNAL);
            } else {
                edge = new Edge(tr.getVert1().getX(), tr.getVert1().getY(), tr.getVert2().getX(), tr.getVert2().getY(), Rasterizer.EdgeType.TRANSITION);
            }
            edgeTable.add(edge);
        }
        log.debug("Edge table size: " + edgeTable.size());
        return edgeTable;
    }

}

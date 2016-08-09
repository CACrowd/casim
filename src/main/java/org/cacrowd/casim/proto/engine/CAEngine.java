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

package org.cacrowd.casim.proto.engine;


import com.google.inject.Inject;
import com.vividsolutions.jts.geom.Envelope;
import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimconnector.engine.CAAgentFactory;
import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.SimulationEngine;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.output.CAScenarioWriter;
import org.cacrowd.casim.proto.geom.Edge;
import org.cacrowd.casim.proto.geom.Rasterizer;
import org.cacrowd.casim.proto.scenario.ProtoCAScenario;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import proto.HybridSimProto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laemmel on 05/05/16.
 */
public class CAEngine {

    private static final Logger log = Logger.getLogger(CAEngine.class);


    @Inject
    private ProtoCAScenario protoCAScenario;

    @Inject
    private EventsManager env;

    @Inject
    private CAAgentFactory agentFactory;


    private Map<Id<CAEnvironment>, SimulationEngine> enginesCA = new HashMap<>();
    private double simCATime;

    public void doSimStep(double time) {
        double stepDuration = Constants.CA_STEP_DURATION;
        //Log.log("------> BEGINNING STEPS AT "+time);
        for (; this.simCATime < time; this.simCATime += stepDuration) {
            for (SimulationEngine engine : this.enginesCA.values()) {
//                double currentTime = System.currentTimeMillis();
                engine.doSimStep(this.simCATime);
//                double afterTime = System.currentTimeMillis();
//				qSim.getEventsManager().processEvent(new CAEngineStepPerformedEvent(this.simCATime, (float)(afterTime-currentTime), engine.getAgentGenerator().getContext().getPopulation().size()));
            }
        }
    }

    public void prepareSim(HybridSimProto.Scenario request) {

        initEnvironment(request);

        generateCAEngines();

        initGenerators();


        //on prepare


//        try {
//            grid.saveCSV("/Users/laemmel/tmp/");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("done");
    }

    private void initGenerators() {
        for (Id<CAEnvironment> key : this.enginesCA.keySet()) {
            agentFactory.addAgentsGenerator(key, this.enginesCA.get(key).getAgentGenerator());
        }
    }

    private void generateCAEngines() {
        for (CAEnvironment env : this.protoCAScenario.getEnvironments().values()) {
            createAndAddEngine(env);
        }
    }

    private void createAndAddEngine(CAEnvironment env) {
        SimulationEngine engine = new SimulationEngine(env.getContext());

        //TODO: extract CAAgentMover from matsimconnector or replace
        engine.setAgentMover(new CAAgentMoverProto(this, env.getContext(), env));
        this.enginesCA.put(env.getId(), engine);
    }

    private void initEnvironment(HybridSimProto.Scenario request) {
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
        int rows = (int) (envelope.getHeight() / Constants.CA_CELL_SIDE) + 3;
        int cols = (int) (envelope.getWidth() / Constants.CA_CELL_SIDE) + 3;

        EnvironmentGrid grid = new EnvironmentGrid(rows, cols, envelope.getMinX() - Constants.CA_CELL_SIDE, envelope.getMinY() - Constants.CA_CELL_SIDE);
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

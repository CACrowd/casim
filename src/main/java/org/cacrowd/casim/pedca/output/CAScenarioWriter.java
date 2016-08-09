package org.cacrowd.casim.pedca.output;
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

import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by laemmel on 18/05/16.
 */
public class CAScenarioWriter {


    private final EnvironmentGrid grid;

    public CAScenarioWriter(EnvironmentGrid grid) {

        this.grid = grid;

    }

    public void write(String file) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file)));
//        bw.append("[\n");
        JSONArray array = new JSONArray();
//        for (CAEnvironment env : sc.getEnvironments().values()) {
            for (int i = grid.getRows() - 1; i >= 0; i--) {
                for (int j = 0; j < grid.getColumns(); j++) {

                    GridPoint gp = new GridPoint(j, i);
                    Coordinate coord = grid.gridPoint2Coordinate(gp);
                    JSONObject obj = new JSONObject();
                    obj.put("kind", grid.getCellValue(i, j));
                    obj.put("x", coord.getX() - grid.getOffsetX());
                    obj.put("y", coord.getY() - grid.getOffsetY());


//                    obj.put("coordinate",coord);
                    obj.put("width", Constants.CA_CELL_SIDE);
//                    obj.write(bw);
//                    bw.append(",\n");
                    array.put(obj);
                }
//            }


        }
        array.write(bw);
//        bw.append("]");
        bw.close();

    }
}

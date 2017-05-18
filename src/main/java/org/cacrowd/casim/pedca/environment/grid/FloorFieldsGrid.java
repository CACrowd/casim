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

package org.cacrowd.casim.pedca.environment.grid;

import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.utility.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class FloorFieldsGrid extends Grid<Double> {

    private final EnvironmentGrid environment;
    private int levels;

    public FloorFieldsGrid(int rows, int cols) {
        super(rows, cols);
        this.levels = 0;
        environment = new EnvironmentGrid(rows, cols);
    }

    public FloorFieldsGrid(EnvironmentGrid environment) {
        super(environment.getRows(), environment.getColumns());
        this.environment = environment;
        this.levels = 0;
    }

    public FloorFieldsGrid(EnvironmentGrid environment, MarkerConfiguration markerConfiguration) {
        this(environment);
        for (Destination destination : markerConfiguration.getTacticalDestinations()) {
            generateField(destination);
        }
    }

    private void generateField(Destination destArea) {
        int fieldLevel = levels;
        destArea.setLevel(fieldLevel);
        ArrayList<GridPoint> L = new ArrayList<GridPoint>();
        for (int i = 0; i < destArea.size(); i++) {
            L.add(destArea.get(i));
            setCellValue(fieldLevel, L.get(i), 0);
        }

        while (L.size() != 0) {
            GridPoint pivot = L.get(0);
            double pivotValue = getCellValue(fieldLevel, pivot);
            L.remove(0); //TODO slow operation on array lists, consider using linked list instead [GL May '17]
            Neighbourhood N = getNeighbourhood(pivot);
            for (int i = 0; i < N.size(); i++) {
                GridPoint neighbour = N.get(i);
                int x = neighbour.getX();
                int y = neighbour.getY();

                double newvalue = pivotValue;
                double nvalue = getCellValue(fieldLevel, neighbour);

                if (Math.abs(x - pivot.getX()) + Math.abs(y - pivot.getY()) == 2)
                    newvalue += Constants.SQRT2;
                else
                    newvalue += 1;
                if ((nvalue == Constants.MAX_FF_VALUE || nvalue > newvalue)) {
                    setCellValue(fieldLevel, new GridPoint(x, y), newvalue);
                    if ((pivotValue == 0) || (!environment.belongsToTacticalDestination(pivot) || environment.belongsToTacticalDestination(neighbour)))
                        L.add(neighbour);
                }
            }
        }
        this.levels++;
    }

    protected boolean neighbourCondition(int row, int col) {
        return super.neighbourCondition(row, col) && environment.isWalkable(row, col);
    }

    private void setCellValue(int fieldLevel, GridPoint gridPoint, double i) {
        get(gridPoint).set(fieldLevel, i, true);
    }

    public Double getCellValue(int fieldLevel, GridPoint gridPoint) {
        Double result = get(gridPoint).get(fieldLevel);
        if (result == null)
            return Constants.MAX_FF_VALUE;
        return result;
    }

    @Override
    public void saveCSV(String path) throws IOException {
        path = path + "/environment/floorFields";
        new File(path).mkdirs();
        for (int level = 0; level < this.levels; level++) {
            File file = new File(path + "/floorField_" + level + ".csv");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (int i = getRows() - 1; i >= 0; i--) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < getColumns(); j++)
                    line.append(getCellValue(level, new GridPoint(j, i))).append(",");
                line.append("\n");
                bw.write(line.toString());
            }
            bw.close();
        }
    }

    @Override
    protected void loadFromCSV(File file) {
        // TODO Auto-generated method stub

    }
}

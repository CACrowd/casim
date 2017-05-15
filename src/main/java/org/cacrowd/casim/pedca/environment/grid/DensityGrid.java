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

import org.cacrowd.casim.pedca.environment.grid.neighbourhood.PedestrianFootprint;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.pedca.utility.Distances;

import java.io.File;
import java.io.IOException;


public class DensityGrid extends Grid<Double> {

    private static double cellArea = Math.pow(Constants.CELL_SIZE, 2);
    private final PedestrianFootprint pedestrianFootprint;
    private final EnvironmentGrid environmentGrid;

    public DensityGrid(int rows, int cols, EnvironmentGrid environmentGrid) {
        super(rows, cols);
        this.pedestrianFootprint = new PedestrianFootprint(Constants.DENSITY_GRID_RADIUS);
        this.environmentGrid = environmentGrid;
    }

    protected void diffuse(GridPoint position) {
        for (GridPoint shift : pedestrianFootprint.getValuesMap().keySet()) {
            GridPoint positionToWrite = Distances.gridPointDifference(position, shift);
            if (neighbourCondition(positionToWrite.getY(), positionToWrite.getX())) {
                Double oldValue = get(positionToWrite).get(0);
                if (oldValue == null)
                    get(positionToWrite).add(pedestrianFootprint.getValuesMap().get(shift));
                else
                    get(positionToWrite).set(0, oldValue + pedestrianFootprint.getValuesMap().get(shift));
            }
        }
    }

    protected void remove(GridPoint position) {
        for (GridPoint shift : pedestrianFootprint.getValuesMap().keySet()) {
            GridPoint positionToWrite = Distances.gridPointDifference(position, shift);
            if (neighbourCondition(positionToWrite.getY(), positionToWrite.getX())) {
                Double oldValue = get(positionToWrite).get(0);
                get(positionToWrite).set(0, oldValue - pedestrianFootprint.getValuesMap().get(shift));
            }
        }
    }

    public double getDensityAt(GridPoint position) {
        if (Constants.DENSITY_GRID_RADIUS == 0)
            return Constants.GLOBAL_DENSITY;
        double deltaArea = 0;
        for (GridPoint shift : pedestrianFootprint.getValuesMap().keySet()) {
            GridPoint positionToWrite = Distances.gridPointDifference(position, shift);
            if (!neighbourCondition(positionToWrite.getY(), positionToWrite.getX())) {
                deltaArea += cellArea;
            }
        }
        double densityValue;
        if (get(position).get(0) != null)
            densityValue = get(position).get(0);
        else
            densityValue = 0;
        double footprintArea = pedestrianFootprint.getArea();
        densityValue = densityValue * footprintArea / (footprintArea - deltaArea);
        return densityValue;
    }

    @Override
    public boolean neighbourCondition(int row, int col) {
        if (environmentGrid != null)
            return super.neighbourCondition(row, col) && environmentGrid.isWalkable(row, col);
        else
            return super.neighbourCondition(row, col);
    }

    @Override
    protected void loadFromCSV(File file) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveCSV(String path) throws IOException {
        // TODO Auto-generated method stub

    }


}

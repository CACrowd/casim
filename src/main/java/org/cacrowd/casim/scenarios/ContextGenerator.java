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

package org.cacrowd.casim.scenarios;

import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfigurationImpl;
import org.cacrowd.casim.pedca.environment.markers.Start;
import org.cacrowd.casim.pedca.utility.Constants;

import java.io.File;
import java.io.IOException;


public class ContextGenerator {

    public static Context createContextWithResourceEnvironmentFileV2(String envFileName) {
        EnvironmentGrid environmentGrid = null;
        MarkerConfiguration markerConfiguration = null;
        try {
            File environmentFile = new File(Constants.RESOURCE_PATH + "/" + envFileName);
            environmentGrid = new EnvironmentGrid(environmentFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        markerConfiguration = EnvironmentGenerator.searchFinalDestinations(environmentGrid);
        EnvironmentGenerator.addTacticalDestinations(markerConfiguration, environmentGrid);
        return new Context(environmentGrid, markerConfiguration);
    }

    public static Context createContextWithResourceEnvironmentFile(String path) {
        EnvironmentGrid environmentGrid = null;
        MarkerConfiguration markerConfiguration = null;
        try {
            File environmentFile = new File(Constants.RESOURCE_PATH + "/" + Constants.ENVIRONMENT_FILE);
            environmentGrid = new EnvironmentGrid(environmentFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        markerConfiguration = EnvironmentGenerator.generateBorderDestinations(environmentGrid);
        EnvironmentGenerator.addTacticalDestinations(markerConfiguration, environmentGrid);
        Context context = new Context(environmentGrid, markerConfiguration);
        try {
            context.saveConfiguration(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return context;
    }

    public static Context createAndSaveBidCorridorContext(String path, int rows, int cols) {
        Context context = getBidCorridorContext(rows, cols);
        try {
            context.saveConfiguration(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return context;
    }

    public static Context createAndSaveBottleneckContext(String path, float sizeX, float sizeY, float bottleneckWidth, float bottleneckHeight, float bottleneckPosY) {
        Context context = getBottleneckContext(sizeX, sizeY, bottleneckWidth, bottleneckHeight, bottleneckPosY);
        try {
            context.saveConfiguration(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return context;
    }

    private static Context getBottleneckContext(float sizeX, float sizeY, float bottleneckWidth, float bottleneckHeight, float bottleneckPosY) {
        EnvironmentGrid environmentGrid = new EnvironmentGrid((int) (sizeY / Constants.CELL_SIZE), (int) (sizeX / Constants.CELL_SIZE));
        EnvironmentGenerator.initBottleneckScenario(environmentGrid, bottleneckWidth, bottleneckHeight, bottleneckPosY);
        MarkerConfiguration markerConfiguration = EnvironmentGenerator.generateBorderDestinations(environmentGrid);
        EnvironmentGenerator.addTacticalDestinations(markerConfiguration, environmentGrid);
        return new Context(environmentGrid, markerConfiguration);
    }

    private static Context getBidCorridorContext(int rows, int cols) {
        EnvironmentGrid environmentGrid = new EnvironmentGrid(rows, cols);
        EnvironmentGenerator.initCorridorWithWalls(environmentGrid, false);
        MarkerConfiguration markerConfiguration = EnvironmentGenerator.generateBorderDestinations(environmentGrid);
        return new Context(environmentGrid, markerConfiguration);
    }

    public static Context getCorridorContext(int rows, int cols, int populationSize) {
        EnvironmentGrid environmentGrid = new EnvironmentGrid(rows, cols);
        EnvironmentGenerator.initCorridorWithWalls(environmentGrid, false);
        MarkerConfiguration markerConfiguration = new MarkerConfigurationImpl();
        markerConfiguration.addDestination(EnvironmentGenerator.getCorridorEastDestination(environmentGrid));
        Start start = EnvironmentGenerator.getCorridorWestStart(environmentGrid);
        start.setTotalPedestrians(populationSize);
        markerConfiguration.addStart(start);
        return new Context(environmentGrid, markerConfiguration);
    }
}

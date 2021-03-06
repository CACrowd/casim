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

package org.cacrowd.casim.pedca.context;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.cacrowd.casim.pedca.agents.ActivePopulation;
import org.cacrowd.casim.pedca.environment.grid.DensityGrid;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.FloorFieldsGrid;
import org.cacrowd.casim.pedca.environment.grid.PedestrianGrid;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfigurationImpl;
import org.cacrowd.casim.pedca.environment.network.CANetwork;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.pedca.io.MarkerConfigurationReader;
import org.cacrowd.casim.pedca.io.MarkerConfigurationWriter;

import java.io.*;
import java.util.ArrayList;

@Singleton
public class Context {
    double timeOfDay = 0;
    private Coordinate environmentOrigin = new Coordinate(0, 0);    //shift of this context with respect to the coordinate system of the scenario
    private double environmentRotation = 0;    //only for visualization purposes
    private ArrayList<PedestrianGrid> pedestrianGrids;
    private EnvironmentGrid environmentGrid;
    private FloorFieldsGrid floorFieldsGrid;
    private MarkerConfiguration markerConfiguration;
    private ActivePopulation population;
    private CANetwork network;
    private int iteration = 0;

    @Inject
    public Context() {
        population = new ActivePopulation();
    }

    public Context(EnvironmentGrid environmentGrid, MarkerConfiguration markerConfiguration) {
        initializeGrids(environmentGrid, markerConfiguration);
        population = new ActivePopulation();
        network = new CANetwork(markerConfiguration, floorFieldsGrid);
    }


    public Context(String path) throws IOException, ClassNotFoundException {
        MarkerConfiguration mc = new MarkerConfigurationImpl();
        new MarkerConfigurationReader(mc).loadConfiguration(path);
        initializeGrids(environmentGrid, markerConfiguration);
        population = new ActivePopulation();
        network = new CANetwork(markerConfiguration, floorFieldsGrid);
        loadCoordinates(path);
    }

    public void initialize(EnvironmentGrid grid, MarkerConfiguration markerConfiguration) {
        initializeGrids(grid, markerConfiguration);
//        population = new ActivePopulation();
        network = new CANetwork(markerConfiguration, floorFieldsGrid);
    }

    public double getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(double timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    private void initializeGrids(EnvironmentGrid environmentGrid, MarkerConfiguration markerConfiguration) {
        this.environmentGrid = environmentGrid;
        this.markerConfiguration = markerConfiguration;
        floorFieldsGrid = new FloorFieldsGrid(environmentGrid, markerConfiguration);
        pedestrianGrids = new ArrayList<PedestrianGrid>();
        pedestrianGrids.add(new PedestrianGrid(environmentGrid.getRows(), environmentGrid.getColumns(), environmentGrid));
    }

    public void saveConfiguration(String path) throws IOException {
        new MarkerConfigurationWriter(markerConfiguration).saveConfiguration(path);
        environmentGrid.saveCSV(path);
        floorFieldsGrid.saveCSV(path);
        saveCoordinates(path);
    }

    private void loadCoordinates(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path + "/coordinates.txt"));
        String line = br.readLine();
        String coordString = line.substring(line.indexOf(":") + 1);
        environmentOrigin = new Coordinate(Double.parseDouble(coordString.substring(0, coordString.indexOf(","))), Double.parseDouble(coordString.substring(coordString.indexOf(",") + 1)));

        line = br.readLine();
        environmentRotation = Double.parseDouble(line.substring(line.indexOf(":") + 1));

        br.close();
    }

    private void saveCoordinates(String path) throws IOException {
        File file = new File(path + "/coordinates.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        String line = "coord:" + environmentOrigin.getX() + "," + environmentOrigin.getY() + "\n";
        bw.write(line);
        line = "rotation:" + environmentRotation;
        bw.write(line);
        bw.close();
    }

    public EnvironmentGrid getEnvironmentGrid() {
        return environmentGrid;
    }

    public void setEnvironmentGrid(EnvironmentGrid environmentGrid) {
        this.environmentGrid = environmentGrid;
    }

    public FloorFieldsGrid getFloorFieldsGrid() {
        return floorFieldsGrid;
    }

    public ArrayList<PedestrianGrid> getPedestrianGrids() {
        return pedestrianGrids;
    }

    public PedestrianGrid getPedestrianGrid() {
        return pedestrianGrids.get(0);
    }

    public DensityGrid getDensityGrid() {
        return pedestrianGrids.get(0).getDensityGrid();
    }

    public ActivePopulation getPopulation() {
        return population;
    }

    public MarkerConfiguration getMarkerConfiguration() {
        return markerConfiguration;
    }

    public CANetwork getNetwork() {
        return network;
    }

    public int getRows() {
        return environmentGrid.getRows();
    }

    public int getColumns() {
        return environmentGrid.getColumns();
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }
}

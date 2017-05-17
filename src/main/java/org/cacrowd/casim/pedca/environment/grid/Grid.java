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

import org.apache.log4j.Logger;
import org.cacrowd.casim.pedca.environment.grid.neighbourhood.Neighbourhood;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.pedca.utility.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Grid<T> {

    private static final Logger log = Logger.getLogger(Grid.class);

    protected ArrayList<ArrayList<GridCell<T>>> cells;
    private double offsetY = 0.;
    private double offsetX = 0.;

    Grid(int rows, int cols, double offsetX, double offsetY) {

        this.offsetX = offsetX;
        this.offsetY = offsetY;
        initGrid(rows, cols);
    }

    public Grid(int rows, int cols) {
        this(rows, cols, 0, 0);
    }

    Grid(File file) throws IOException {
        initEmptyGrid();
        loadFromCSV(file);
    }

    Grid(String fileName) throws IOException {
        initEmptyGrid();
        loadFromCSV(fileName);
    }


    private void initEmptyGrid() {
        cells = new ArrayList<ArrayList<GridCell<T>>>();
    }

    private void initGrid(int rows, int cols) {
        cells = new ArrayList<ArrayList<GridCell<T>>>();
        for (int i = 0; i < rows; i++) {
            addRow();
            for (int j = 0; j < cols; j++)
                addElementAt(i);
        }
    }

    public Coordinate rowCol2Coordinate(int row, int col) {
        GridPoint gp = new GridPoint(col, row);
        return gridPoint2Coordinate(gp);
    }

    public Coordinate gridPoint2Coordinate(GridPoint gp) {
        return new Coordinate(gp.getX() * Constants.CELL_SIZE + offsetX, gp.getY() * Constants.CELL_SIZE + offsetY);
    }

    public GridPoint coordinate2GridPoint(Coordinate c) {
        int col = (int) ((c.getX() - offsetX) / Constants.CELL_SIZE);
        int row = (int) ((c.getY() - offsetY) / Constants.CELL_SIZE);
        return new GridPoint(col, row);

    }

    public double getOffsetX() {
        return this.offsetX;
    }

    public double getOffsetY() {
        return this.offsetY;
    }

    public int y2Row(double y) {
        return (int) ((y - offsetY) / Constants.CELL_SIZE);
    }

    public int x2Col(double x) {
        return (int) ((x - offsetX) / Constants.CELL_SIZE);
    }

    public void add(int i, int j, T object) {
        get(i, j).add(object);
    }

    public GridCell<T> get(GridPoint p) {

        return cells.get(p.getY()).get(p.getX());
    }

    public GridCell<T> get(int row, int col) {
        if (row == -1 || col == -1) {
            log.error("-1");
        }

        return cells.get(row).get(col);
    }

    void addRow() {
        cells.add(new ArrayList<GridCell<T>>());
    }

    private void addElementAt(int row) {
        cells.get(row).add(new GridCell<T>());
    }

    void addElementAt(int row, T object) {
        GridCell<T> cell = new GridCell<T>();
        cell.add(object);
        cells.get(row).add(cell);
    }

    public int getRows() {
        return cells.size();
    }

    public int getColumns() {
        return cells.get(0).size();
    }

    public ArrayList<T> getObjectsAt(int i, int j) {
        return get(i, j).getObjects();
    }

    /**
     * Return Moore neighbourhood of gp, excluding cells over the boundaries of the grid
     */
    public Neighbourhood getNeighbourhood(GridPoint gp) {
        Neighbourhood neighbourhood = new Neighbourhood();
        final int radius = 1;
        int row_gp = gp.getY();
        int col_gp = gp.getX();
        for (int row = row_gp - radius; row <= row_gp + radius; row++)
            for (int col = col_gp - radius; col <= col_gp + radius; col++)
                if (neighbourCondition(row, col))// &&(row==row_gp||col==col_gp)) for the Von Neumann neighbourhood
                    neighbourhood.add(new GridPoint(col, row));
        return neighbourhood;
    }

    public String toString() {
        StringBuilder res = new StringBuilder();
        for (ArrayList<GridCell<T>> cell : cells) {
            for (GridCell<T> aCell : cell) res.append(aCell.toString()).append(" ");
            res.append("\n");
        }
        return res.toString();
    }

    protected boolean neighbourCondition(int row, int col) {
        return row >= 0 && col >= 0 && col < getColumns() && row < getRows();
    }

    protected abstract void loadFromCSV(File file) throws IOException;

    private void loadFromCSV(String fileName) throws IOException {
        File environmentFile = new File(fileName);
        loadFromCSV(environmentFile);
    }

    public abstract void saveCSV(String path) throws IOException;

}

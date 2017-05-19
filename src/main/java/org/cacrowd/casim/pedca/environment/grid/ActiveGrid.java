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

package org.cacrowd.casim.pedca.environment.grid;

import java.io.File;
import java.io.IOException;


public abstract class ActiveGrid<T> extends Grid<T> {
    protected int step;

    public ActiveGrid(int rows, int cols) {
        super(rows, cols);
        step = 0;
    }

    public ActiveGrid(String fileName) throws IOException {
        super(fileName);
        // TODO Auto-generated constructor stub
    }

    public void step() {
        updateGrid();
        step++;
    }

    protected abstract void updateGrid();

    @Override
    protected void loadFromCSV(File file) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveCSV(String path) throws IOException {
        // TODO Auto-generated method stub

    }
}

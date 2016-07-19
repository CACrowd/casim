package proto.geom;
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

import matsimconnector.utility.Constants;
import org.junit.Before;
import org.junit.Test;
import pedca.environment.grid.EnvironmentGrid;
import pedca.output.CAScenarioWriter;

import java.io.IOException;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

/**
 * Created by laemmel on 13/07/16.
 */
public class RasterizerTest {


    private static final int[][] REF_SIMPLE_TRIANGLE = {
            {0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };

    private static final int[][] REF_CONCAVE_WITH_HOLES = {
            {0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 0, 0, -1, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1, -1, -1},
            {0, 0, 0, -1, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, -1, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, 0, 0, 0, 0, 0, -1},
            {0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, 0, 0, 0, 0, -1},
            {0, 0, 0, 0, 0, 0, 0, -2, -2, -2, -2, 0, 0, 0, 0, -1},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {-2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2, -2}
    };


    @Test
    public void testSimpleTriangle() {
        LinkedList<Edge> et = new LinkedList<>();
        Edge e0 = new Edge(0, 0, 0, 5, Rasterizer.Kind.WALL);
        et.add(e0);
        Edge e1 = new Edge(0, 5, 5, 4, Rasterizer.Kind.WALL);
        et.add(e1);
        Edge e2 = new Edge(5, 4, 0, 0, Rasterizer.Kind.WALL);
        et.add(e2);

        int rows = (int) (5 / Constants.CA_CELL_SIDE) + 1;
        int cols = (int) (5 / Constants.CA_CELL_SIDE) + 1;
        EnvironmentGrid grid = new EnvironmentGrid(rows, cols, 0, 0);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid.setCellValue(row, col, -1);
            }
        }
        Rasterizer r = new Rasterizer(grid);
        r.rasterize(et);

//        try {
//            new CAScenarioWriter(grid).write("src/main/js/grid.json");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            grid.saveCSV("/Users/laemmel/tmp/");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                assertThat(grid.getCellValue(row, col), is(equalTo(REF_SIMPLE_TRIANGLE[12 - row][col])));
            }
        }
    }

    @Test
    public void testConcaveWithHole() {
        LinkedList<Edge> et = new LinkedList<>();
        Edge e0 = new Edge(0, 0, 0, 10, Rasterizer.Kind.WALL);
        et.add(e0);
        Edge e1 = new Edge(0, 10, 2.5, 7, Rasterizer.Kind.WALL);
        et.add(e1);
        Edge e2 = new Edge(2.5, 7, 4, 9, Rasterizer.Kind.WALL);
        et.add(e2);
        Edge e3 = new Edge(4, 9, 6, 0, Rasterizer.Kind.WALL);
        et.add(e3);
        Edge e4 = new Edge(6, 0, 0, 0, Rasterizer.Kind.TRANSITION);
        et.add(e4);

        Edge e5 = new Edge(2.5, 1, 4.5, 1, Rasterizer.Kind.TRANSITION);
        et.add(e5);
        Edge e6 = new Edge(2.5, 1, 2.5, 5, Rasterizer.Kind.WALL);
        et.add(e6);
        Edge e7 = new Edge(2.5, 5, 4, 4, Rasterizer.Kind.WALL);
        et.add(e7);
        Edge e8 = new Edge(4, 4, 4.5, 1, Rasterizer.Kind.WALL);
        et.add(e8);

        int rows = (int) (10 / Constants.CA_CELL_SIDE) + 1;
        int cols = (int) (6 / Constants.CA_CELL_SIDE) + 1;
        EnvironmentGrid grid = new EnvironmentGrid(rows, cols, 0, 0);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                grid.setCellValue(row, col, -1);
            }
        }
        Rasterizer r = new Rasterizer(grid);
        r.rasterize(et);
        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                assertThat(grid.getCellValue(row, col), is(equalTo(REF_CONCAVE_WITH_HOLES[25 - row][col])));
            }
        }
//        try {
//            new CAScenarioWriter(grid).write("src/main/js/grid.json");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


}
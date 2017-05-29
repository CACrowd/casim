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

package org.cacrowd.casim.utility.rasterizer;


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

/**
 * Created by laemmel on 13/07/16.
 */
public class RasterizerTest {


    private static final int[][] REF_SIMPLE_TRIANGLE = {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {-1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };

    private static final int[][] REF_CONCAVE_WITH_HOLES = {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1},
            {-1, 0, 0, 0, 0, 0, -3, -3, -3, -3, -3, -3, 0, 0, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, 0, 0, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, 0, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -1, -2, -2, 0, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, -1, -2, -2, 0, 0, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -1, 0, -2, 0, 0, 0, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, -2, -2, 0, 0, 0, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, -2, 0, 0, 0, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -2, -2, 0, 0, 0, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, -2, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, -1, -1, -1, -1, 0, 0, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, -1, -1, -1},
            {-1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };

    private static final int[][] REF_RECT = {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };

    @Test
    public void testMarkerConfiguruations() {
        LinkedList<Edge> et = new LinkedList<>();
        Edge e0 = new Edge(0, 0, 0, 0, 2.4, Rasterizer.EdgeType.WALL);
        et.add(e0);
        Edge e0a = new Edge(1, 0.4, 0.41, 0.4, 1.99, Rasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0a);

        List<GridPoint> dest1Ref = new ArrayList<>();
        dest1Ref.add(new GridPoint(1, 1));
        dest1Ref.add(new GridPoint(1, 2));
        dest1Ref.add(new GridPoint(1, 3));
        dest1Ref.add(new GridPoint(1, 4));


        Edge e0b = new Edge(2, 2., 0.41, 2., 1.99, Rasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0b);

        List<GridPoint> dest2Ref = new ArrayList<>();
        dest2Ref.add(new GridPoint(5, 1));
        dest2Ref.add(new GridPoint(5, 2));
        dest2Ref.add(new GridPoint(5, 3));
        dest2Ref.add(new GridPoint(5, 4));

        Edge e0c = new Edge(3, 14., 0.41, 14., 1.99, Rasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0c);

        List<GridPoint> dest3Ref = new ArrayList<>();
        dest3Ref.add(new GridPoint(35, 1));
        dest3Ref.add(new GridPoint(35, 2));
        dest3Ref.add(new GridPoint(35, 3));
        dest3Ref.add(new GridPoint(35, 4));

        Edge e0d = new Edge(4, 15.6, 0.41, 15.6, 1.99, Rasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(e0d);

        List<GridPoint> dest4Ref = new ArrayList<>();
        dest4Ref.add(new GridPoint(39, 1));
        dest4Ref.add(new GridPoint(39, 2));
        dest4Ref.add(new GridPoint(39, 3));
        dest4Ref.add(new GridPoint(39, 4));

        Edge e1 = new Edge(0, 0, 2.4, 16, 2.4, Rasterizer.EdgeType.WALL);
        et.add(e1);
        Edge e2 = new Edge(0, 16, 2.4, 16, 0, Rasterizer.EdgeType.WALL);
        et.add(e2);
        Edge e3 = new Edge(0, 16, 0, 0, 0, Rasterizer.EdgeType.WALL);
        et.add(e3);


        Injector injector = Guice.createInjector();
        injector.getInstance(Rasterizer.class).buildContext(et);
        MarkerConfiguration markerConfiguration = injector.getInstance(Context.class).getMarkerConfiguration();
        ArrayList<Destination> destinations = markerConfiguration.getTacticalDestinations();
        assertThat(destinations.size(), is(equalTo(4)));

        List<GridPoint> dest1 = markerConfiguration.getDestination(1).getCells();
        assertThat(dest1.size(), is(equalTo(dest1Ref.size())));
        assertThat(dest1, containsInAnyOrder(dest1Ref.toArray()));

        List<GridPoint> dest2 = markerConfiguration.getDestination(2).getCells();
        assertThat(dest2.size(), is(equalTo(dest2Ref.size())));
        assertThat(dest2, containsInAnyOrder(dest2Ref.toArray()));

        List<GridPoint> dest3 = markerConfiguration.getDestination(3).getCells();
        assertThat(dest3.size(), is(equalTo(dest3Ref.size())));
        assertThat(dest3, containsInAnyOrder(dest3Ref.toArray()));

        List<GridPoint> dest4 = markerConfiguration.getDestination(4).getCells();
        assertThat(dest4.size(), is(equalTo(dest4Ref.size())));
        assertThat(dest4, containsInAnyOrder(dest4Ref.toArray()));


    }

    @Test
    public void testRect() {
        LinkedList<Edge> et = new LinkedList<>();
        Edge e0 = new Edge(0, 0, 0, 0, 2.4, Rasterizer.EdgeType.WALL);
        et.add(e0);
        Edge e1 = new Edge(1, 0, 2.4, 4, 2.4, Rasterizer.EdgeType.WALL);
        et.add(e1);
        Edge e2 = new Edge(2, 4, 2.4, 4, 0, Rasterizer.EdgeType.WALL);
        et.add(e2);
        Edge e3 = new Edge(3, 4, 0, 0, 0, Rasterizer.EdgeType.WALL);
        et.add(e3);

        Injector injector = Guice.createInjector();
        injector.getInstance(Rasterizer.class).buildContext(et);
        EnvironmentGrid grid = injector.getInstance(Context.class).getEnvironmentGrid();

        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getColumns(); col++) {

                assertThat(grid.getCellValue(row, col), is(equalTo(REF_RECT[row][col])));
            }
        }
    }


    @Test
    public void testSimpleTriangle() {
        LinkedList<Edge> et = new LinkedList<>();
        Edge e0 = new Edge(0, 0, 0, 0, 5, Rasterizer.EdgeType.WALL);
        et.add(e0);
        Edge e1 = new Edge(1, 0, 5, 5, 4, Rasterizer.EdgeType.WALL);
        et.add(e1);
        Edge e2 = new Edge(2, 5, 4, 0, 0, Rasterizer.EdgeType.WALL);
        et.add(e2);

        Injector injector = Guice.createInjector();
        injector.getInstance(Rasterizer.class).buildContext(et);
        EnvironmentGrid grid = injector.getInstance(Context.class).getEnvironmentGrid();

        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                assertThat(grid.getCellValue(row, col), is(equalTo(REF_SIMPLE_TRIANGLE[row][col])));
            }
        }
    }

    @Test
    public void testConcaveWithHole() {
        LinkedList<Edge> et = new LinkedList<>();
        Edge e0 = new Edge(0, 0, 0, 0, 10, Rasterizer.EdgeType.WALL);
        et.add(e0);
        Edge e1 = new Edge(1, 0, 10, 2.5, 7, Rasterizer.EdgeType.WALL);
        et.add(e1);
        Edge e2 = new Edge(2, 2.5, 7, 4, 9, Rasterizer.EdgeType.WALL);
        et.add(e2);
        Edge e3 = new Edge(3, 4, 9, 6, 0, Rasterizer.EdgeType.WALL);
        et.add(e3);
        Edge e4 = new Edge(4, 6, 0, 0, 0, Rasterizer.EdgeType.WALL);
        et.add(e4);

        Edge e5 = new Edge(5, 2.5, 1, 4.5, 1, Rasterizer.EdgeType.TRANSITION);
        et.add(e5);
        Edge e6 = new Edge(6, 2.5, 1, 2.5, 5, Rasterizer.EdgeType.WALL);
        et.add(e6);
        Edge e7 = new Edge(7, 2.5, 5, 4, 4, Rasterizer.EdgeType.WALL);
        et.add(e7);
        Edge e8 = new Edge(8, 4, 4, 4.5, 1, Rasterizer.EdgeType.WALL);
        et.add(e8);

        Edge tr = new Edge(9, 2.5, 7, 4, 4, Rasterizer.EdgeType.TRANSITION_INTERNAL);
        et.add(tr);

        Injector injector = Guice.createInjector();
        injector.getInstance(Rasterizer.class).buildContext(et);
        EnvironmentGrid grid = injector.getInstance(Context.class).getEnvironmentGrid();

        for (int row = 0; row < grid.getRows(); row++) {
            for (int col = 0; col < grid.getColumns(); col++) {
                assertThat(grid.getCellValue(row, col), is(equalTo(REF_CONCAVE_WITH_HOLES[row][col])));
            }
        }


    }


}

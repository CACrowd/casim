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

package org.cacrowd.casim.pedca.utility;
/****************************************************************************/
/****************************************************************************/
//
//   This file is part of casim.
//   casim is free software: you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation, either version 2 of the License, or
//   (at your option) any later version.
//
/****************************************************************************/

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Created by laemmel on 09/08/16.
 */
public class DistancesTests {

    private static final double EPSILON = 0.0001;

    @Test
    public void testEuclideanDistanceGP() {
        GridPoint p1 = new GridPoint(10, 10);
        GridPoint p2 = new GridPoint(20, 20);

        double dist = Distances.EuclideanDistance(p1, p2);

        assertThat(dist, is(closeTo(14.14213562373095048801, EPSILON)));

    }

    @Test
    public void testEuclideanDistanceCoord() {
        Coordinate c1 = new Coordinate(10, 10);
        Coordinate c2 = new Coordinate(20, 20);

        double dist = Distances.EuclideanDistance(c1, c2);

        assertThat(dist, is(closeTo(14.14213562373095048801, EPSILON)));
    }

    @Test
    public void testGridPointDifference() {
        GridPoint p1 = new GridPoint(10, 10);
        GridPoint p2 = new GridPoint(20, 30);

        GridPoint res = Distances.gridPointDifference(p1, p2);

        assertThat(res.getX(), is(-10));
        assertThat(res.getY(), is(-20));

    }

    @Test
    public void testGridPointSum() {
        GridPoint p1 = new GridPoint(10, 10);
        GridPoint p2 = new GridPoint(20, 30);

        GridPoint res = Distances.gridPointSum(p1, p2);

        assertThat(res.getX(), is(30));
        assertThat(res.getY(), is(40));
    }
}

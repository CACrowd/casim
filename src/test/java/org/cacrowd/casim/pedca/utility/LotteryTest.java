

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

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.WeightedCell;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;


/**
 * Created by laemmel on 05/05/16.
 */
public class LotteryTest {

    private static final int SEED = 42;

    private static double frstRndm;
    private static double scndRndm;
    private static double thrdRndm;

    @BeforeClass
    public static void runBeforeClass() {
        CASimRandom.reset(SEED);
        frstRndm = CASimRandom.nextDouble();
        scndRndm = CASimRandom.nextDouble();
        thrdRndm = CASimRandom.nextDouble();
    }

    @Before
    public void runBefore() {
        CASimRandom.reset(SEED);
    }

    @Test
    public void testSimpleExtraction() {
        double prb1 = frstRndm * 0.9;
        double prb2 = scndRndm * 1.1;

        boolean res1 = Lottery.simpleExtraction(prb1);
        assertThat(res1, is(false)); //neighborhood + center
        boolean res2 = Lottery.simpleExtraction(prb2);
        assertThat(res2, is(true));
    }

    @Test
    public void testPickWinnerThird() {
        ArrayList<WeightedCell> weightedCells = new ArrayList<>();
        weightedCells.add(new WeightedCell(new GridPoint(1, 1), frstRndm / 3));
        weightedCells.add(new WeightedCell(new GridPoint(2, 2), frstRndm / 3));
        WeightedCell wc = new WeightedCell(new GridPoint(3, 1), frstRndm / 2);
        weightedCells.add(wc);
        WeightedCell res = Lottery.pickWinner(weightedCells);

        assertThat(res, is(wc));
    }

    @Test
    public void testPickWinnerNull() {
        ArrayList<WeightedCell> weightedCells = new ArrayList<>();
        weightedCells.add(new WeightedCell(new GridPoint(1, 1), frstRndm / 3));
        weightedCells.add(new WeightedCell(new GridPoint(2, 2), frstRndm / 3));
        WeightedCell wc = new WeightedCell(new GridPoint(3, 1), frstRndm / 4);
        weightedCells.add(wc);
        WeightedCell res = Lottery.pickWinner(weightedCells);

        assertThat(res, is(nullValue()));
    }

    @Test
    public void testNormalizeProbabilities() {
        ArrayList<WeightedCell> weightedCells = new ArrayList<>();
        weightedCells.add(new WeightedCell(new GridPoint(1, 1), frstRndm));
        weightedCells.add(new WeightedCell(new GridPoint(2, 2), scndRndm));
        WeightedCell wc = new WeightedCell(new GridPoint(3, 1), thrdRndm);
        weightedCells.add(wc);
        double probSum = frstRndm + scndRndm + thrdRndm;
        Lottery.normalizeProbabilities(weightedCells, probSum);
        assertThat(weightedCells.get(0).getP(), is(frstRndm / probSum));
        assertThat(weightedCells.get(1).getP(), is(scndRndm / probSum));
        assertThat(weightedCells.get(2).getP(), is(thrdRndm / probSum));
    }

    @Test
    public void testSort() {
        ArrayList<WeightedCell> weightedCells = new ArrayList<>();
        WeightedCell wc0 = new WeightedCell(new GridPoint(1, 1), 0.1);
        weightedCells.add(wc0);
        WeightedCell wc1 = new WeightedCell(new GridPoint(2, 2), 0.9);
        weightedCells.add(wc1);
        WeightedCell wc2 = new WeightedCell(new GridPoint(3, 1), 0.05);
        weightedCells.add(wc2);

        Lottery.sort(weightedCells);

        assertThat(weightedCells, contains(wc2, wc0, wc1));

    }
}

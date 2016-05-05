package pedca.utility;
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import pedca.environment.grid.GridPoint;
import pedca.environment.grid.WeightedCell;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;


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
}

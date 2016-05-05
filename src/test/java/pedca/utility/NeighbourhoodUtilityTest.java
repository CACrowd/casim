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


import org.junit.Test;
import pedca.environment.grid.GridPoint;
import pedca.environment.grid.neighbourhood.Neighbourhood;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

/**
 * Created by laemmel on 03/05/16.
 */
public class NeighbourhoodUtilityTest {


	@Test
	public void testCalculateMoorNeighbourhood() {
		GridPoint neighbour = new GridPoint(5,9);
		Neighbourhood nb = NeighbourhoodUtility.calculateMooreNeighbourhood(neighbour);

		assertThat(nb.size(),is(equalTo(9))); //neighborhood + center

		assertThat(nb.getObjects(),containsInAnyOrder(new GridPoint(4,9),new GridPoint(5,9),new GridPoint(6,9),
				new GridPoint(5,10),new GridPoint(5,8),new GridPoint(4,10),new GridPoint(6,10), new GridPoint(4,8),new GridPoint(6,8)));
	}

	@Test
	public void testCalculateVonNeumannNeighbourhood() {
		GridPoint neighbour = new GridPoint(8,9);
		Neighbourhood nb = NeighbourhoodUtility.calculateVonNeumannNeighbourhood(neighbour);



		assertThat(nb.size(),is(equalTo(5))); //neighborhood + center



		assertThat(nb.getObjects(),containsInAnyOrder(new GridPoint(8,9),new GridPoint(8,10),new GridPoint(8,8),new GridPoint(7,9),new GridPoint(9,9)));

	}
}

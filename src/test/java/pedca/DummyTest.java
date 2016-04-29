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


package pedca;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Just a dumy test as place holder and to test travis-ci
 * Created by laemmel on 29.04.16.
 */
public class DummyTest {

	@Test
	public void floatingPointNumbersTest03() {
		assertThat(5.6+5.8,closeTo(11.4,0.000001));
	}
}

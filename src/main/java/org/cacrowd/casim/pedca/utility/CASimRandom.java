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

import java.util.Random;

public abstract class CASimRandom {

    public static Random generator = new Random(Constants.RANDOM_SEED);

    public static double nextDouble() {
        return generator.nextDouble();
    }

    public static int nextInt(int limit) {
        return generator.nextInt(limit);
    }

    public static void reset(long seed) {
        generator = new Random(seed);
    }

}

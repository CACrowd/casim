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

public class Constants {
    //General useful constants
    public static final double SQRT2 = Math.sqrt(2);
    //Constants for environment
    public static final int ENV_OBSTACLE = -1;
    public static final int ENV_TACTICAL_DESTINATION = -2;
    public static final int ENV_FINAL_DESTINATION = -3;
    public static final int ENV_STAIRS_BORDER = -4;
    public static final int ENV_DELAYED_DESTINATION = -5;   //destination with additional time to cross it
    public static final int ENV_CONSTRAINED_DESTINATION = -6;
    public static final int ENV_SCHEDULED_DESTINATION = -7;  //destination with scheduled opening time (for boarding procedures)
    public static final int ENV_WALKABLE_CELL = 0;
    public static final double MAX_FF_VALUE = Double.POSITIVE_INFINITY;
    //Constants for Conflict Management
    public static final double FRICTION_PROBABILITY = 0.;
    public static final double CELL_SIZE = 0.4;
    public static final double STEP_DURATION = 0.3;
    public static final int SHADOWS_LIFE = 2;
    public static final double SHADOWS_PROBABILITY = 1.;
    public static final int STEP_FOR_BIDIRECTIONAL_SWAPPING = 2;
    public static final String RESOURCE_PATH = "/some/path";
    public static final String ENVIRONMENT_FILE = "some file";


    //Constant for the random seed
    public static long RANDOM_SEED = 42;
    public static double DENSITY_GRID_RADIUS = 2;


    //Constants for Pedestrian Model
    public static Double KS = 6.0;
    public static Double PHI = 1.0;

    //TODO don't know what this means [GL May 17]
    /**
     * global density value used to efficiently compute the test of the fundamental diagram.
     * Used by DensityGrid only if Constants.DENSITY_GRID_RADIUS==0
     **/
    public static double GLOBAL_DENSITY;
    public static boolean stopOnStairs;
}

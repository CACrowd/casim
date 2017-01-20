/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.matsimconnector.utility;

import java.util.ArrayList;
import java.util.List;

public class Constants {

	public static final String CA_MOBSIM_MODE = "MobsimCA";
	public static final String CA_LINK_MODE = "walkCA";
	public static final String WALK_LINK_MODE = "walk";
	public static final String CAR_LINK_MODE = "car";
	public static final String TO_Q_LINK_MODE = "CA->Q";
	public static final String TO_CA_LINK_MODE = "Q->CA";
	public static final double CA_CELL_SIDE = 0.4;
	public static final double CA_STEP_DURATION = .3;
	public static final double PEDESTRIAN_SPEED = CA_CELL_SIDE / CA_STEP_DURATION;
	/**
	 * name to use to add CAScenario to a matsim scenario as a scenario element
	 **/
	public static final String CASCENARIO_NAME = "CAScenario";
	public static final double TRANSITION_AREA_LENGTH = CA_CELL_SIDE * 5;
	public static final Double TRANSITION_LINK_LENGTH = TRANSITION_AREA_LENGTH / 2.;
	public static final int TRANSITION_AREA_COLUMNS = (int) (TRANSITION_AREA_LENGTH / CA_CELL_SIDE);
	public static final String RESOURCE_PATH = "src/main/resources";
	public static final String COORDINATE_SYSTEM = "EPSG:3395";
	public static String[] ORIGIN_FLOWS = {"n","e","w","s"};    //each char denote one origin of flow (e.g. "e" stays for "east")
	public static boolean stopOnStairs;
	/** this is for the generation of the fundamental diagram of the CA: pedestrian will be kept inside the
	 * CAEnvironment until this time (in seconds). Keep to 0 if you want to run normal simulation.**/
	public static int CA_FD_TEST_END_TIME = 0; //1200;
	public static double SIMULATION_DURATION = 22000;
	public static int SIMULATION_ITERATIONS = 30;
	/**
	 * global density value used to efficiently compute the test of the fundamental diagram.
	 * Used by DensityGrid only if Constants.DENSITY_GRID_RADIUS==0
	 * **/
	public static double GLOBAL_DENSITY;
	public static Double FLOPW_CAP_PER_METER_WIDTH = 1000.;
	public static Double FAKE_LINK_WIDTH = 10.;  // 1.2;
	public static Double CA_LINK_LENGTH = 10.;
	public static boolean MARGINAL_SOCIAL_COST_OPTIMIZATION = false;
	
	public static String PATH;
	static {
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("win") >= 0)
			PATH = "C:/Users/Luca/Documents/uni/Dottorato/Juelich/developing_stuff/Test";
		else
			PATH = "/tmp/TestCA";
		FD_TEST_PATH = PATH+"/FD/";
	}

	public static final String DEBUG_TEST_PATH = PATH+"/debug";
	public static String FD_TEST_PATH;
	public static String INPUT_PATH = DEBUG_TEST_PATH+"/input";
	public static String OUTPUT_PATH = DEBUG_TEST_PATH+"/output";
	public static String ENVIRONMENT_FILE = "environmentGrid_Braess.csv";
	public static boolean BRAESS_WL = false;
	public static boolean VIS = true;
	public static boolean SAVE_FRAMES = false;
	public static List<String> stairsLinks;


	static{
		stairsLinks = new ArrayList<String>();
	}
}

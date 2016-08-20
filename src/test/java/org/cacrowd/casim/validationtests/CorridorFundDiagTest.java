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

package org.cacrowd.casim.validationtests;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimconnector.run.FunDiagSimRunner;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class CorridorFundDiagTest {

    private static final double scenarioLength = 10.;
    private static final double acceptedError = 0.05;
    private static String [] origin1Dir = {"w"};
	private static String [] origins2Dir = {"w", "e"};
	private static double tic = 0.25;
	private static double maxDensity = 1./Math.pow(Constants.CA_CELL_SIDE,2);
	private static String ORIGINAL_FD_PATH;
    private static Level LOG_LEVEL;

	public static void main(String [] args){
		Constants.stairsLinks.add("HybridNode_0-->HybridNode_1");
		Constants.stairsLinks.add("HybridNode_1-->HybridNode_0");

		String FD_PATH = ""+Constants.FD_TEST_PATH;
		setupCommonConstants();
		Constants.FD_TEST_PATH += "1Dir/";
		Constants.ORIGIN_FLOWS = origin1Dir;
		new CorridorFundDiagTest().generateFD();

		Constants.FD_TEST_PATH = FD_PATH;
		setupCommonConstants();
		Constants.FD_TEST_PATH += "2Dir/";
		Constants.ORIGIN_FLOWS = origins2Dir;
		new CorridorFundDiagTest().generateFD();
	}
	
	private static void setupCommonConstants() {
		Constants.CA_FD_TEST_END_TIME = 2000;
		Constants.SIMULATION_DURATION = 2300;
        Constants.FAKE_LINK_WIDTH = 3.2;   //width of the scenario. The final width of the corridor is 0.8m lower since border rows are filled with obstacles
        Constants.CA_LINK_LENGTH = scenarioLength;
        org.cacrowd.casim.pedca.utility.Constants.DENSITY_GRID_RADIUS = 0;  //to consider only global density during the simulation
        Constants.VIS = false;
	}

    @BeforeClass
    public static void disableVerboseLogging() {
        LOG_LEVEL = Logger.getRootLogger().getLevel();
        Logger.getRootLogger().setLevel(Level.WARN);
    }

    @AfterClass
    public static void restoreLogLevel() {
        Logger.getRootLogger().setLevel(LOG_LEVEL);
    }

	private static void compareValues(double[] avgValuesTest, File resTarget)
            throws IOException {
        int[] countValues;
		BufferedReader br;
		String line;
		br = new BufferedReader(new FileReader(resTarget));
		br.readLine();		//first line contains labels
		line = br.readLine();
		double [] avgValuesTarget = new double[(int)(maxDensity/tic)];
		countValues = new int[avgValuesTarget.length];
		for(int i=0;i<avgValuesTarget.length;i++){
			avgValuesTarget[i]=0;
			countValues[i]=0;
		}
		boolean end = false;
		while (!end && line!=null){
			StringTokenizer st = new StringTokenizer(line,"\t");
			double density = Double.parseDouble(st.nextToken());
			double travelTime = Double.parseDouble(st.nextToken());
			if (density<=maxDensity){
				double flow = (10/travelTime)*density;
				int index = (int)(density/tic) - 1;
				avgValuesTarget[index]+=flow;
				countValues[index]++;
                line = br.readLine();
            }else{
				end = true;
			}
		}
		br.close();
		for(int i=0;i<avgValuesTarget.length;i++){
			if (avgValuesTest[i]>0){
				avgValuesTarget[i]/=countValues[i];
				assertThat(avgValuesTest[i],closeTo(avgValuesTarget[i],acceptedError));
			}
		}
	}

    private static void loadData(File res, double[] avgValuesTest) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(res));
		br.readLine();		//first line describes the labels
		String line = br.readLine();

        int [] countValues = new int[avgValuesTest.length];
		for(int i=0;i<avgValuesTest.length;i++){
			avgValuesTest[i]=0;
			countValues[i]=0;
		}

        while (line!=null){
			StringTokenizer st = new StringTokenizer(line,",");
			double density = Double.parseDouble(st.nextToken());
			double travelTime = Double.parseDouble(st.nextToken());
			double flow = (scenarioLength/travelTime)*density;
			int index = (int)(density/tic) - 1;
			avgValuesTest[index]+=flow;
			countValues[index]++;
            line = br.readLine();
        }
		br.close();
		for(int i=0;i<avgValuesTest.length;i++){
			avgValuesTest[i]/=countValues[i];
		}
    }

    public void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }

    private void generateFD() {

        for (double density = tic; density <= maxDensity; density += tic) {
            Constants.GLOBAL_DENSITY = density;
            FunDiagSimRunner runner = new FunDiagSimRunner(density, null);
            runner.generateScenario();
            runner.runSimulation();
        }
    }

    @Test
    public void checkFD_1Dir() {
        try {
            ORIGINAL_FD_PATH = "" + Constants.FD_TEST_PATH;
            setupCommonConstants();
            Constants.FD_TEST_PATH += "1Dir/";
            Constants.ORIGIN_FLOWS = origin1Dir;
            generateFD();
            File res = new File(Constants.FD_TEST_PATH + "fd_data.csv");
            double[] avgValuesTest = new double[(int) (maxDensity / tic)];
            loadData(res, avgValuesTest);
            File resTarget = new File(Constants.RESOURCE_PATH + "/funDiag_1dir.csv");
            compareValues(avgValuesTest, resTarget);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void checkFD_2Dir() {
        try {
            ORIGINAL_FD_PATH = "" + Constants.FD_TEST_PATH;
            setupCommonConstants();
            Constants.FD_TEST_PATH += "2Dir/";
            Constants.ORIGIN_FLOWS = null;
            Constants.ORIGIN_FLOWS = origins2Dir;
            generateFD();
            File res = new File(Constants.FD_TEST_PATH + "fd_data.csv");
            double[] avgValuesTest = new double[(int) (maxDensity / tic)];
            loadData(res, avgValuesTest);
            File resTarget = new File(Constants.RESOURCE_PATH + "/funDiag_2dir.csv");
            compareValues(avgValuesTest, resTarget);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void clean() {
        deleteDirectory(new File(Constants.FD_TEST_PATH));
        Constants.FD_TEST_PATH = "" + ORIGINAL_FD_PATH;
    }
}
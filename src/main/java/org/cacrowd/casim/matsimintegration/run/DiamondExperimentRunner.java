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

package org.cacrowd.casim.matsimintegration.run;

import com.google.inject.Singleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cacrowd.casim.hybridsim.run.HybridsimServer;
import org.cacrowd.casim.matsimintegration.hybridsim.run.RunDiamondNashExperiment;
import org.cacrowd.casim.matsimintegration.hybridsim.run.RunMultiScaleDiamondNashExperiment;

import java.io.IOException;

import static java.lang.System.exit;


@Singleton
public class DiamondExperimentRunner {

    private static RunType runType;
    private static double BOTTLENECK_WIDTH;

    public static void main(String[] args) throws InterruptedException {

        if (args.length != 1) {
            printUsage();
            exit(-1);
        }

//        if (!args[1].equals("0.4") && !args[1].equals("0.8") && !args[1].equals("1.2")) {
//            printUsage();
//            exit(-1);
//        }
//        BOTTLENECK_WIDTH = Double.parseDouble(args[1]);


        if (args[0].equalsIgnoreCase("nash")) {
            System.out.println("Running Nash equilibrium experiment");
            runType = RunType.Nash;
        } else if (args[0].equalsIgnoreCase("ms_nash")) {
            System.out.println("Running multi-scale Nash equilibrium experiment with bottleneck width: " + Double.parseDouble(args[1]));
            runType = RunType.MultiScaleNash;
        }

        Logger.getRootLogger().setLevel(Level.INFO);

        Thread t1 = new Thread(new DiamondExperimentRunner.Server());
        Thread t2 = new Thread(new DiamondExperimentRunner.Client());


        t1.start();
        Thread.sleep(2000); //give server some time to start
        t2.start();
    }

    private static void printUsage() {
        System.out.println("Runner to run diamond-setup experiments");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("\tDiamondExperimentRunner Nash <bottlneck width>");
        System.out.println("\tDiamondExperimentRunner MS_Nash <bottlneck width>");
        System.out.println("(<bottlneck width> must be one of: {0.4, 0.8, 1.2})");
    }

    private enum RunType {Nash, MultiScaleNash}

    private static final class Server implements Runnable {


        @Override
        public void run() {
            try {
                HybridsimServer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final class Client implements Runnable {

        @Override
        public void run() {
            try {
                if (runType == RunType.Nash) {
                    RunDiamondNashExperiment.run(30., 20., 1.2);
                } else if (runType == RunType.MultiScaleNash) {
                    RunMultiScaleDiamondNashExperiment.run(30., 20., 1.2);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

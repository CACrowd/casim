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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.cacrowd.casim.hybridsim.run.HybridsimServer;
import org.cacrowd.casim.matsimintegration.hybridsim.run.RunDaganzoMSCBExperiment;
import org.cacrowd.casim.matsimintegration.hybridsim.run.RunDaganzoNashExperiment;

import java.io.IOException;

import static java.lang.System.exit;

// Runner to repeat the Daganzo Bottleneck experiments as discussed in Section 4.1 in
// Crociani, L. & Lämmel, G.: Multidestination Pedestrian Flows in Equilibrium: A Cellular Automaton-Based Approach.
// Computer-Aided Civil and Infrastructure Engineering 00 (2016) 1–17
// DOI: 10.1111/mice.12209
public class DaganzoExperimentRunner {

    private static boolean RUN_MSCB = false;
    private static double BOTTLENECK_WIDTH;

    public static void main(String[] args) throws InterruptedException {

        if (args.length != 2) {
            printUsage();
            exit(-1);
        }

        if (!args[1].equals("0.4") && !args[1].equals("0.8") && !args[1].equals("0.4")) {
            printUsage();
            exit(-1);
        }
        BOTTLENECK_WIDTH = Double.parseDouble(args[1]);


        if (args[0].equalsIgnoreCase("nash")) {
            System.out.println("Running Nash equilibrium experiment with bottleneck width: " + Double.parseDouble(args[1]));

        } else {
            RUN_MSCB = true;
            System.out.println("Running MSCB approach experiment with bottleneck width: " + Double.parseDouble(args[1]));
        }

        Logger.getRootLogger().setLevel(Level.INFO);

        Thread t1 = new Thread(new DaganzoExperimentRunner.Server());
        Thread t2 = new Thread(new DaganzoExperimentRunner.Client());


        t1.start();
        Thread.sleep(2000); //give server some time to start
        t2.start();
    }

    private static void printUsage() {
        System.out.println("Runner to repeat the Daganzo Bottleneck experiments as discussed in Section 4.1 in\n" +
                "Crociani, L. & Lämmel, G.: Multidestination Pedestrian Flows in Equilibrium: A Cellular Automaton-Based Approach.\n" +
                "Computer-Aided Civil and Infrastructure Engineering 00 (2016) 1–17\n" +
                "DOI: 10.1111/mice.12209");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("\tDaganzoExperimentRunner Nash <bottlneck width>");
        System.out.println("\tDaganzoExperimentRunner MSCB <bottlneck width>");
        System.out.println("(<bottlneck width> must be one of: {0.4, 0.8, 1.2})");
    }

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
                if (RUN_MSCB) {
                    RunDaganzoMSCBExperiment.run(BOTTLENECK_WIDTH);
                } else {
                    RunDaganzoNashExperiment.run(BOTTLENECK_WIDTH);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

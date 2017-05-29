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

package org.cacrowd.casim.hybridsim.run;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class MyHybridsimRunner {

    public static void main(String[] args) throws InterruptedException {
        Logger.getRootLogger().setLevel(Level.INFO);

        Thread t1 = new Thread(new Server());
        Thread t2 = new Thread(new Client());


        t1.start();
        Thread.sleep(2000); //give server some time to start
        t2.start();
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
            HybridsimTestClient.main(null);
        }
    }
}

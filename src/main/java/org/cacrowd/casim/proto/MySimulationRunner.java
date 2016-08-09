package org.cacrowd.casim.proto;
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by laemmel on 08/08/16.
 */
public class MySimulationRunner {


    public static void main(String[] args) throws IOException, InterruptedException {
        Logger.getRootLogger().setLevel(Level.INFO);

        Thread t1 = new Thread(new CARunner());
        Thread t2 = new Thread(new MATSimRunner());

        t1.start();
        t2.start();

    }

    private static final class MATSimRunner implements Runnable {


        @Override
        public void run() {
            try {
                org.matsim.contrib.hybridsim.run.Example.main(new String[0]);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final class CARunner implements Runnable {

        @Override
        public void run() {
            new ProtoController();
        }
    }
}

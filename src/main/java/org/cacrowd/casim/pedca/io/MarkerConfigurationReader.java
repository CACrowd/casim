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

package org.cacrowd.casim.pedca.io;


import org.cacrowd.casim.pedca.environment.markers.Destination;
import org.cacrowd.casim.pedca.environment.markers.FinalDestination;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.environment.markers.Start;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by laemmel on 16/08/16.
 */
public class MarkerConfigurationReader {

    private final MarkerConfiguration mc;

    public MarkerConfigurationReader(MarkerConfiguration mc) {
        this.mc = mc;
    }

    public void loadConfiguration(String path) throws IOException, ClassNotFoundException {
        path = path + "/markers";
        int countFiles = new File(path + "/starts").listFiles().length;
        FileInputStream streamIn;
        ObjectInputStream ois;

        for (int i = 0; i < countFiles; i++) {
            streamIn = new FileInputStream(path + "/starts/start_" + i + ".ser");
            ois = new ObjectInputStream(streamIn);
            mc.addStart((Start) ois.readObject());
            ois.close();
        }

        countFiles = new File(path + "/destinations").listFiles().length;
        for (int i = 0; i < countFiles; i++) {
            try {
                streamIn = new FileInputStream(path + "/destinations/destination_" + i + ".ser");
                ois = new ObjectInputStream(streamIn);
                mc.addTacticalDestination((Destination) ois.readObject());
            } catch (IOException e) {
                streamIn = new FileInputStream(path + "/destinations/tacticalDestination_" + i + ".ser");
                ois = new ObjectInputStream(streamIn);
                mc.addTacticalDestination((FinalDestination) ois.readObject());
            }
            ois.close();
        }
    }

}

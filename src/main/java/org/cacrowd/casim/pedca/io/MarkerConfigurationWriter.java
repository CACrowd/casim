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


import org.cacrowd.casim.pedca.environment.markers.FinalDestination;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.utility.FileUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by laemmel on 16/08/16.
 */
public class MarkerConfigurationWriter {
    private final MarkerConfiguration mc;

    public MarkerConfigurationWriter(MarkerConfiguration mc) {
        this.mc = mc;
    }

    public void saveConfiguration(String path) throws IOException {
        path = path + "/markers";
        FileUtility.deleteDirectory(new File(path));
        new File(path + "/starts").mkdirs();
        new File(path + "/destinations").mkdirs();
        FileOutputStream fout;
        ObjectOutputStream oos;

        for (int i = 0; i < mc.getStarts().size(); i++) {
            File file = new File(path + "/starts/start_" + i + ".ser");
            file.createNewFile();
            fout = new FileOutputStream(path + "/starts/start_" + i + ".ser", false);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(mc.getStarts().get(i));
            oos.close();
        }

        for (int i = 0; i < mc.getDestinations().size(); i++) {
            if (mc.getDestinations().get(i) instanceof FinalDestination) {
                File file = new File(path + "/destinations/tacticalDestination_" + i + ".ser");
                file.createNewFile();
                fout = new FileOutputStream(path + "/destinations/tacticalDestination_" + i + ".ser", false);
            } else {
                File file = new File(path + "/destinations/destination_" + i + ".ser");
                file.createNewFile();
                fout = new FileOutputStream(path + "/destinations/destination_" + i + ".ser", false);
            }
            oos = new ObjectOutputStream(fout);
            oos.writeObject(mc.getDestinations().get(i));
            oos.close();
        }

    }
}

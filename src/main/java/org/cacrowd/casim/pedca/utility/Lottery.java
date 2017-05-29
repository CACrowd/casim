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

import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.WeightedCell;

import java.util.ArrayList;
import java.util.Comparator;

public class Lottery {

    public static boolean simpleExtraction(double probability) {
        return CASimRandom.nextDouble() <= probability;
    }

    public static WeightedCell pickWinner(ArrayList<WeightedCell> pValues) {
        Double p = CASimRandom.nextDouble();
        Double cumulativeProbability = 0.0;
        for (WeightedCell wc : pValues) {
            cumulativeProbability += wc.getP();
            if (p <= cumulativeProbability) {
                return wc;
            }
        }
        return null;
    }

    public static void normalizeProbabilities(ArrayList<WeightedCell> probabilityValues, double probabilitySum) {
        for (WeightedCell wc : probabilityValues) {
            probabilityValues.set(probabilityValues.indexOf(wc), new WeightedCell(new GridPoint(wc.getX(), wc.getY()), wc.getP() / probabilitySum));

        }
    }

    //TODO never used --> remove or use [gl May 2016]
    public static void sort(ArrayList<WeightedCell> wc) {
        wc.sort(new Comparator<WeightedCell>() {
            @Override
            public int compare(WeightedCell arg0, WeightedCell arg1) {
                if (arg0.getP() < arg1.getP())
                    return -1;
                else if (arg0.getP() == arg1.getP())
                    return 0;
                else return 1;
            }

        });
    }


    public static <T> T extractObject(ArrayList<T> objects) {
        return extractObjects(objects, 1).get(0);
    }


    public static <T> ArrayList<T> extractObjects(ArrayList<T> objects, int howMany) {
        if (howMany >= objects.size())
            return objects;
        ArrayList<T> extracted = new ArrayList<T>();
        @SuppressWarnings("unchecked")
        ArrayList<T> cellsCopy = (ArrayList<T>) objects.clone();
        for (int i = 0; i < howMany; i++) {
            int extracted_index = (int) (CASimRandom.nextDouble() * cellsCopy.size()); //TODO why not using CASimRandom.nextInt(cellsCopy.size()) instead?
            //TODO also, it might be faster to have a List<Integer> with elements 0,1,2 ... max_expected_cells and the each time this method is called perform
            //TODO a random permutation (Collections.shuffle) and use the first ``howMany'' elements as indexes for the copy operation [GL May 2016]


            extracted.add(cellsCopy.get(extracted_index));
            cellsCopy.remove(extracted_index);
        }
        return extracted;
    }

    public static DirectionUtility.Heading extractHeading() {
        int extracted_index = (int) (CASimRandom.nextDouble() * DirectionUtility.Heading.values().length);
        if (DirectionUtility.Heading.values()[extracted_index] == DirectionUtility.Heading.X)
            extracted_index--;
        return DirectionUtility.Heading.values()[extracted_index];
    }
}

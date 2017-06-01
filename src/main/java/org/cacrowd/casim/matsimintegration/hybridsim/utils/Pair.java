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

package org.cacrowd.casim.matsimintegration.hybridsim.utils;

public class Pair<T, T1> {

    private final T t1;
    private final T t2;

    public Pair(T t1, T t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public int hashCode() {
        return (t1.hashCode() << 16) + t2.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pair && ((Pair) obj).t1 == t1 && ((Pair) obj).t2 == t2;
    }
}

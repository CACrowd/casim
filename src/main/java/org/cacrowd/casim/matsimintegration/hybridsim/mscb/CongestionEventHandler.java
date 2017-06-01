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

package org.cacrowd.casim.matsimintegration.hybridsim.mscb;

import org.matsim.core.events.handler.EventHandler;

/**
 * Created by laemmel on 15/12/2016.
 */
public interface CongestionEventHandler extends EventHandler {

    void handleEvent(CongestionEvent congestionEvent);
}

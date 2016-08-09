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

package org.cacrowd.casim.matsimconnector.congestionpricing;

import org.matsim.core.events.handler.EventHandler;

/**
 * A common congestion pricing handler to handle different implementations of congestion pricing schemes.
 * Available options are -
 *
 * <list>
 * <li> implv3
 * <li> implv4
 * <li> implv6
 * </list>
 * 
 * @author ihab
 */

public interface CongestionEventHandler extends EventHandler {

	void handleEvent(CongestionEvent event);
	
}

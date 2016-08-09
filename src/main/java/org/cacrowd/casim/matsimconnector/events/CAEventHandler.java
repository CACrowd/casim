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

package org.cacrowd.casim.matsimconnector.events;

import org.matsim.core.events.handler.EventHandler;

public interface CAEventHandler extends EventHandler {

	void handleEvent(CAAgentConstructEvent event);

	void handleEvent(CAAgentMoveEvent event);

	void handleEvent(CAAgentExitEvent event);

	void handleEvent(CAAgentMoveToOrigin event);

	void handleEvent(CAAgentLeaveEnvironmentEvent event);

	void handleEvent(CAAgentEnterEnvironmentEvent event);

	void handleEvent(CAAgentChangeLinkEvent event);

	void handleEvent(CAEngineStepPerformedEvent event);
}

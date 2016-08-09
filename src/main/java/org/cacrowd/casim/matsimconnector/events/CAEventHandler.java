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

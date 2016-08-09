package org.cacrowd.casim.pedca.agents;

import org.cacrowd.casim.pedca.environment.grid.GridPoint;

public abstract class PhysicalObject {
	protected GridPoint position;
	
	public GridPoint getPosition() {
		return position;
	}
}

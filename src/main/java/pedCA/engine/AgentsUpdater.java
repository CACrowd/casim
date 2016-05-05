package pedca.engine;

import pedca.agents.Agent;
import pedca.agents.Population;

public class AgentsUpdater {
	private Population population;
	
	public AgentsUpdater(Population population){
		this.population = population;
	}
	
	public void step(){
		if (!population.isEmpty()){
			for(Agent pedestrian : population.getPedestrians()){
				//TODO maybe here is not the right place for this check 
				if (!pedestrian.isArrived()){
					pedestrian.updateChoice();
				}
			}
		}
	}
}

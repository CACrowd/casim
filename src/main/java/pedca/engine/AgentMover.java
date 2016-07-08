package pedca.engine;

import org.apache.log4j.Logger;
import pedca.agents.Agent;
import pedca.agents.Population;
import pedca.context.Context;

public class AgentMover {

    private static final Logger log = Logger.getLogger(AgentMover.class);

    //private final Context context;
    private final Population population;
	
	public AgentMover(Context context){
		//this.context = context;
		this.population = context.getPopulation();
	}
	
	public void step(){
		for(int index=0; index<population.size(); index++){
			Agent pedestrian = population.getPedestrian(index);
			if (pedestrian.isArrived()){
                log.info(pedestrian.toString() + " exited.");
                moveToUniverse(pedestrian);
				index--;
			}
			else{
				pedestrian.move();
			}
		}
	}
	
	//FOR MATSIM CONNECTOR
	public void step(double time){
		throw new RuntimeException("this method is not implemented here");
	}

	private void moveToUniverse(Agent pedestrian) {
		pedestrian.leavePedestrianGrid();
		population.remove(pedestrian);
	}
	
	protected Population getPopulation(){
		return population;
	}
}

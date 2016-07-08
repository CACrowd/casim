package pedca.engine;

import matsimconnector.agents.Pedestrian;
import connector.environment.TransitionArea;
import org.apache.log4j.Logger;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicle;
import pedca.agents.Agent;
import pedca.agents.Population;
import pedca.context.Context;
import pedca.environment.grid.GridPoint;
import pedca.environment.grid.PedestrianGrid;
import pedca.environment.markers.Destination;
import pedca.environment.markers.Start;

import pedca.utility.Lottery;

import java.util.ArrayList;

public class AgentsGenerator {

    private static final Logger log = Logger.getLogger(AgentsGenerator.class);

	private Context context;
	private int pedestrianCounter;
	
	public AgentsGenerator(Context context){
		this.context = context;
		pedestrianCounter = 0;
	}
	
	public void step(){
		for(Start start : getStarts()){
			generateFromStart(start);
		}
	}

	private void generateFromStart(Start start){
		int howMany = start.toBeGenerated();
		ArrayList<GridPoint> usedCells = getPedestrianGrid().getFreePositions(start.getCells());
		if (howMany>usedCells.size()){
            log.warn("not enough space in start " + start.toString());
        }
		else{
			usedCells = Lottery.extractObjects(usedCells,howMany);
		}
		for(GridPoint p : usedCells){
			generateSinglePedestrian(p);
			start.notifyGeneration();
		}
	}
	
	private void generateSinglePedestrian(GridPoint initialPosition) {
		int pedID = getPopulation().getPedestrians().size();
		Destination destination = getRandomDestination();
		Agent pedestrian = new Agent(pedID,initialPosition,destination,context);
		getPopulation().addPedestrian(pedestrian);
		context.getPedestrianGrid().addPedestrian(initialPosition, pedestrian);
	}

	//FOR MATSIM CONNECTOR
	public Pedestrian generatePedestrian(GridPoint initialPosition, int destinationId, QVehicle vehicle, TransitionArea transitionArea){
		int pedID = pedestrianCounter;
		Destination destination = context.getMarkerConfiguration().getDestination(destinationId);
		Agent agent = new Agent(pedID,initialPosition,destination,context);
		Pedestrian pedestrian = new Pedestrian(agent, vehicle, transitionArea);
		getPopulation().addPedestrian(pedestrian);
		//context.getPedestrianGrid().addPedestrian(initialPosition, pedestrian);
		pedestrianCounter++;
		return pedestrian;
		
	}
	
	//FOR MATSIM CONNECTOR
	public Context getContext(){
		return context;
	}
	
	//FOR MATSIM CONNECTOR
	public GridPoint getFreePosition(int destinationId){
		ArrayList<GridPoint> cells = getContext().getMarkerConfiguration().getDestination(destinationId).getCells();
 		ArrayList<GridPoint> usedCells = getPedestrianGrid().getFreePositions(cells);
 		return Lottery.extractObjects(usedCells,1).get(0);
	}
	
	private Destination getRandomDestination() {
		return Lottery.extractObject(context.getMarkerConfiguration().getDestinations());
	}
	
	private Population getPopulation(){
		return context.getPopulation();
	}

	private ArrayList<Start> getStarts(){
		return context.getMarkerConfiguration().getStarts();
	}
	
	private PedestrianGrid getPedestrianGrid(){
		return context.getPedestrianGrid();
	}
}

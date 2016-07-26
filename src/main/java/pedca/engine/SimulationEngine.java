package pedca.engine;

import pedca.context.Context;

import java.io.IOException;

import matsimconnector.utility.Constants;

public class SimulationEngine {
	private int step;
	private final int finalStep;
	private AgentsGenerator agentGenerator;
	private AgentsUpdater agentUpdater;
	private ConflictSolver conflictSolver;
	private AgentMover agentMover;
	private GridsAndObjectsUpdater activeObjectsUpdater;
	
	public SimulationEngine(int finalStep, Context context){
		step = 1;
		this.finalStep = finalStep;
		agentGenerator = new AgentsGenerator(context);
		agentUpdater = new AgentsUpdater(context.getPopulation());
		conflictSolver = new ConflictSolver(context);
		agentMover = new AgentMover(context);
		activeObjectsUpdater = new GridsAndObjectsUpdater(context);
	}
	
	public SimulationEngine(int finalStep, String path) throws IOException{
		this(finalStep,new Context(path));
	}
	
	public SimulationEngine(Context context){
		this(0,context);
	}
	
	@Deprecated
	private void step(){
		agentGenerator.step();
		agentUpdater.step();
		conflictSolver.step();
		agentMover.step();		
		activeObjectsUpdater.step(step*Constants.CA_STEP_DURATION);
		step++;
	}
	
	
	//FOR MATSIM CONNECTOR
	public void doSimStep(double time){
		//Log.log("STEP at: "+time);
		agentUpdater.step();
		conflictSolver.step();
		agentMover.step(time);		
		activeObjectsUpdater.step(time);
		step++;
	}
	
	//FOR MATSIM CONNECTOR
	public AgentsGenerator getAgentGenerator(){
		return agentGenerator;
	}
	
	//FOR MATSIM CONNECTOR
	public void setAgentMover(AgentMover agentMover){
		this.agentMover = agentMover;
	}
	
	@Deprecated
	public void run(){
		while(step<=finalStep){
			//Log.step(step);
			step();
		}
	}
}

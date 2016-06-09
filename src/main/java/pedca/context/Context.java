package pedca.context;

import java.io.IOException;
import java.util.ArrayList;

import matsimconnector.environment.TransitionArea;
import pedca.agents.Population;
import pedca.environment.grid.DensityGrid;
import pedca.environment.grid.EnvironmentGrid;
import pedca.environment.grid.FloorFieldsGrid;
import pedca.environment.grid.PedestrianGrid;
import pedca.environment.markers.MarkerConfiguration;
import pedca.environment.network.CANetwork;

public class Context {
	private ArrayList<PedestrianGrid> pedestrianGrids;
	private EnvironmentGrid environmentGrid;
	private FloorFieldsGrid floorFieldsGrid;
	private MarkerConfiguration markerConfiguration;
	private Population population;
	private CANetwork network;

	public Context(EnvironmentGrid environmentGrid, MarkerConfiguration markerConfiguration){
		initializeGrids(environmentGrid, markerConfiguration);
		population = new Population();
		network = new CANetwork(markerConfiguration, floorFieldsGrid);
	}
	
	public Context(String path) throws IOException{
		this(new EnvironmentGrid(path),new MarkerConfiguration(path));
	}

	private void initializeGrids(EnvironmentGrid environmentGrid, MarkerConfiguration markerConfiguration) {
		this.environmentGrid = environmentGrid;
		this.markerConfiguration = markerConfiguration;
		floorFieldsGrid = new FloorFieldsGrid(environmentGrid, markerConfiguration);
		pedestrianGrids = new ArrayList<PedestrianGrid>();
		pedestrianGrids.add(new PedestrianGrid(environmentGrid.getRows(), environmentGrid.getColumns(), environmentGrid));
	}
	
	public void saveConfiguration(String path) throws IOException{
		markerConfiguration.saveConfiguration(path);
		environmentGrid.saveCSV(path);
		floorFieldsGrid.saveCSV(path);
    } 
	
	public EnvironmentGrid getEnvironmentGrid() {
		return environmentGrid;
	}

	public FloorFieldsGrid getFloorFieldsGrid() {
		return floorFieldsGrid;
	}

	public ArrayList<PedestrianGrid> getPedestrianGrids() {
		return pedestrianGrids;
	}

	public PedestrianGrid getPedestrianGrid(){
		return pedestrianGrids.get(0);
	}
	
	public DensityGrid getDensityGrid(){
		return pedestrianGrids.get(0).getDensityGrid();
	}
	
	//FOR MATSIM CONNECTOR
	public void registerTransitionArea(TransitionArea transitionArea){
		pedestrianGrids.add(transitionArea);
	}
	
	public Population getPopulation(){
		return population;
	}
	
	public MarkerConfiguration getMarkerConfiguration(){
		return markerConfiguration;
	}
	
	public CANetwork getNetwork(){
		return network;
	}
	
	public int getRows(){
		return environmentGrid.getRows();
	}
	
	public int getColumns(){
		return environmentGrid.getColumns();
	}
}

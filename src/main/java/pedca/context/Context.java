package pedca.context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import pedca.agents.Population;
import pedca.environment.grid.DensityGrid;
import pedca.environment.grid.EnvironmentGrid;
import pedca.environment.grid.FloorFieldsGrid;
import pedca.environment.grid.PedestrianGrid;
import pedca.environment.markers.MarkerConfiguration;
import pedca.environment.network.CANetwork;
import pedca.environment.network.Coordinate;
import connector.environment.TransitionArea;

public class Context {
	private ArrayList<PedestrianGrid> pedestrianGrids;
	private EnvironmentGrid environmentGrid;
	private FloorFieldsGrid floorFieldsGrid;
	private MarkerConfiguration markerConfiguration;
	private Population population;
	private CANetwork network;
	
	public Coordinate environmentOrigin = new Coordinate(0,0);	//shift of this context with respect to the coordinate system of the scenario
	public double environmentRotation = 0;	//only for visualization purposes

	public Context(EnvironmentGrid environmentGrid, MarkerConfiguration markerConfiguration){
		initializeGrids(environmentGrid, markerConfiguration);
		population = new Population();
		network = new CANetwork(markerConfiguration, floorFieldsGrid);
	}
	
	public Context(String path) throws IOException{
		this(new EnvironmentGrid(path),new MarkerConfiguration(path));
		loadCoordinates(path);
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
		saveCoordinates(path);
    } 
	
	public void loadCoordinates(String path) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(path+"/coordinates.txt"));
		String line = br.readLine();
		String coordString = line.substring(line.indexOf(":")+1);
		environmentOrigin = new Coordinate(Double.parseDouble(coordString.substring(0, coordString.indexOf(","))), Double.parseDouble(coordString.substring(coordString.indexOf(",")+1)));
		
		line = br.readLine();
		environmentRotation = Double.parseDouble(line.substring(line.indexOf(":")+1));

		br.close();
	}
	
	public void saveCoordinates(String path) throws IOException{
		File file = new File(path+"/coordinates.txt");
		if (!file.exists()) {
			file.createNewFile();
		} 
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		String line="coord:"+environmentOrigin.getX()+","+environmentOrigin.getY()+"\n";
		bw.write(line);
		line = "rotation:"+environmentRotation;
		bw.write(line);
		bw.close();
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

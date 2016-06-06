package scenarios;

import matsimconnector.utility.Constants;
import pedca.context.Context;
import pedca.environment.grid.EnvironmentGrid;
import pedca.environment.markers.MarkerConfiguration;
import pedca.environment.markers.Start;

import java.io.File;
import java.io.IOException;


public class ContextGenerator {
	
	public static Context createContextWithResourceEnvironmentFile(String path){
		EnvironmentGrid environmentGrid = null;
		MarkerConfiguration markerConfiguration = null;
		try {
//			File environmentFile = new File(Constants.RESOURCE_PATH+"/environmentGrid_Braess.csv");
//			File environmentFile = new File(Constants.RESOURCE_PATH+"/environmentGrid_Braess_WL.csv");
			File environmentFile = new File(Constants.RESOURCE_PATH+"/"+ Constants.ENVIRONMENT_FILE);
			environmentGrid = new EnvironmentGrid(environmentFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		markerConfiguration = EnvironmentGenerator.generateBorderDestinations(environmentGrid);
		EnvironmentGenerator.addTacticalDestinations(markerConfiguration, environmentGrid);
		Context context = new Context(environmentGrid, markerConfiguration);
		try {
			context.saveConfiguration(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return context;
	}
	
	public static Context createAndSaveBidCorridorContext(String path, int rows, int cols){
		Context context = getBidCorridorContext(rows, cols);
		try {
			context.saveConfiguration(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return context;
	}
	
	public static Context createAndSaveBottleneckContext(String path, float sizeX, float sizeY, float bottleneckWidth, float bottleneckHeight, float bottleneckPosY) {
		Context context = getBottleneckContext(sizeX, sizeY, bottleneckWidth, bottleneckHeight, bottleneckPosY);
		try {
			context.saveConfiguration(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return context;
	}
	
	private static Context getBottleneckContext(float sizeX, float sizeY, float bottleneckWidth, float bottleneckHeight, float bottleneckPosY) {
		EnvironmentGrid environmentGrid = new EnvironmentGrid((int)(sizeY/Constants.CA_CELL_SIDE), (int)(sizeX/Constants.CA_CELL_SIDE));
		EnvironmentGenerator.initBottleneckScenario(environmentGrid, bottleneckWidth, bottleneckHeight, bottleneckPosY);
		MarkerConfiguration markerConfiguration = EnvironmentGenerator.generateBorderDestinations(environmentGrid);
		return new Context(environmentGrid, markerConfiguration);
	}
	
	private static Context getBidCorridorContext(int rows, int cols) {
		EnvironmentGrid environmentGrid = new EnvironmentGrid(rows, cols);
		EnvironmentGenerator.initCorridorWithWalls(environmentGrid, false);
		MarkerConfiguration markerConfiguration = EnvironmentGenerator.generateBorderDestinations(environmentGrid);
		return new Context(environmentGrid, markerConfiguration);
	}

	public static Context getCorridorContext(int rows, int cols, int populationSize){
		EnvironmentGrid environmentGrid = new EnvironmentGrid(rows, cols);
		EnvironmentGenerator.initCorridorWithWalls(environmentGrid, false);
		MarkerConfiguration markerConfiguration = new MarkerConfiguration();
		markerConfiguration.addDestination(EnvironmentGenerator.getCorridorEastDestination(environmentGrid));
		Start start = EnvironmentGenerator.getCorridorWestStart(environmentGrid);
		start.setTotalPedestrians(populationSize);
		markerConfiguration.addStart(start);
		return new Context(environmentGrid, markerConfiguration);
	}
}

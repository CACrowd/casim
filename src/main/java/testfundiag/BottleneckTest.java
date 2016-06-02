package testfundiag;

import java.io.File;

import matsimconnector.run.BottleneckTestRunner;
import matsimconnector.utility.Constants;

public class BottleneckTest {
	
	
	public static void main (String [] args){
		setupCommonConstants();
		double tic = 0.4;
		double maxWidth = 5.2;
		for (double w = tic; w<=maxWidth; w+=tic){
			//configuration of the bottleneck scenario from W. Liao et Al. (PED 2014) [height of the bottleneck is approximated to 1.2] LC
			BottleneckTestRunner runner = new BottleneckTestRunner(350, 20.0, 26.0, w, 1.2, 11.8);
			runner.generateScenario();
			runner.runSimulation();
		}
	}
	
	private static void deleteDirectory(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDirectory(f);
	        }
	    }
	    file.delete();
	}
	
	private static void setupCommonConstants() {
		Constants.SIMULATION_DURATION = 2200;
		pedca.utility.Constants.DENSITY_GRID_RADIUS = 1.2;
		Constants.ORIGIN_FLOWS = "s";
		Constants.FLOPW_CAP_PER_METER_WIDTH = 30.;
		Constants.VIS = false;
	}
	
	
	
	//@After
	public void deleteFolder(){
		deleteDirectory(new File(Constants.FD_TEST_PATH));
	}

}

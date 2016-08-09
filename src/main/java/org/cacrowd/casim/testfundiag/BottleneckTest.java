package org.cacrowd.casim.testfundiag;

import matsimconnector.run.BottleneckTestRunner;
import matsimconnector.utility.Constants;

import java.io.File;

public class BottleneckTest {
	
	
	public static void main (String [] args){
		setupCommonConstants();
		float tic = 0.4f;
		float maxWidth = 5.2f;
		for (float w = tic; Math.round(w*10)/10f<=maxWidth; w+=tic){
			//configuration of the bottleneck scenario from W. Liao et Al. (PED 2014) [height of the bottleneck is approximated to 1.2] LC
			BottleneckTestRunner runner = new BottleneckTestRunner(350, 20.0f, 16.0f, w, 1.2f, 11.8f);
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
		Constants.ORIGIN_FLOWS[0] = "s";
		Constants.FLOPW_CAP_PER_METER_WIDTH = 30.;
		Constants.VIS = false;
	}
	
	
	
	//@After
	public void deleteFolder(){
		deleteDirectory(new File(Constants.FD_TEST_PATH));
	}

}

package matsimconnector.run;

import matsimconnector.scenariogenerator.ScenarioGenerator;
import matsimconnector.utility.Constants;

public class LoadAndRunCASimulation {

	public static void main(String[] args) {
		if(args.length != 0 && Boolean.parseBoolean(args[0])){
			Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION = Boolean.parseBoolean(args[0]);
			Constants.OUTPUT_PATH = Constants.DEBUG_TEST_PATH+"/outputSO";
			Constants.INPUT_PATH = Constants.DEBUG_TEST_PATH+"/inputSO";
		}else{
			Constants.OUTPUT_PATH = Constants.DEBUG_TEST_PATH+"/outputNE";
			Constants.INPUT_PATH = Constants.DEBUG_TEST_PATH+"/inputNE";
		}		
				
		ScenarioGenerator.main(new String[0]);
		CASimulationRunner.main(new String[0]);
	}	

}

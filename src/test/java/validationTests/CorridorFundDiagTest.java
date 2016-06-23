package validationTests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import matsimconnector.run.FunDiagSimRunner;
import matsimconnector.utility.Constants;

import org.junit.After;
import org.junit.Test;

public class CorridorFundDiagTest {
	
	private static final double scenarioLength = 10.;
	private static double tic = 0.25;
	private static double maxDensity = 1./Math.pow(Constants.CA_CELL_SIDE,2);
	private static final double acceptedError = 0.05;
	
	public void deleteDirectory(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDirectory(f);
	        }
	    }
	    file.delete();
	}
	
	public static void main(String [] args){
		Constants.stairsLinks.add("HybridNode_0-->HybridNode_1");
		Constants.stairsLinks.add("HybridNode_1-->HybridNode_0");
		
		String FD_PATH = ""+Constants.FD_TEST_PATH;
		setupCommonConstants();
		Constants.FD_TEST_PATH += "1Dir/";
		Constants.ORIGIN_FLOWS = "w";
		new CorridorFundDiagTest().generateFD();
		
		Constants.FD_TEST_PATH = FD_PATH;
		setupCommonConstants();
		Constants.FD_TEST_PATH += "2Dir/";
		Constants.ORIGIN_FLOWS = "we";
		new CorridorFundDiagTest().generateFD();
	}
	
	private void generateFD() {
		for (double density = tic; density<=maxDensity;density+=tic){
			FunDiagSimRunner runner = new FunDiagSimRunner(density, null);
			runner.generateScenario();
			runner.runSimulation();
		}
	}
	
	private static void setupCommonConstants() {
		Constants.CA_TEST_END_TIME = 3600;
		Constants.SIMULATION_DURATION = 3800;
		Constants.FAKE_LINK_WIDTH = 3.2;   //width of the scenario. The final width of the corridor is 0.8m lower since border rows are filled with obstacles 
		Constants.CA_LINK_LENGTH = scenarioLength;
		pedca.utility.Constants.DENSITY_GRID_RADIUS = 10.;
		Constants.VIS = false;
	}
	
	@Test
	public void checkFD_1Dir() {		
		try{
			String FD_PATH = ""+Constants.FD_TEST_PATH;
			setupCommonConstants();
			Constants.FD_TEST_PATH += "1Dir/";
			Constants.ORIGIN_FLOWS = "w";
			generateFD();
			File res = new File(Constants.FD_TEST_PATH+"fd_data.csv");
			Constants.FD_TEST_PATH = FD_PATH;
			double [] avgValuesTest = new double[(int)(maxDensity/tic)];
			loadData(res, avgValuesTest);			
			File resTarget = new File(Constants.RESOURCE_PATH+"/funDiag_1dir.csv");
			compareValues(avgValuesTest, resTarget);
     	}catch(Exception e){
     		e.printStackTrace();
     	}
	}
	
	@Test
	public void checkFD_2Dir() {		
		try{
			String FD_PATH = ""+Constants.FD_TEST_PATH;
			setupCommonConstants();
			Constants.FD_TEST_PATH += "2Dir/";
			Constants.ORIGIN_FLOWS = "we";
			generateFD();
			File res = new File(Constants.FD_TEST_PATH+"fd_data.csv");
			Constants.FD_TEST_PATH = FD_PATH;
			double [] avgValuesTest = new double[(int)(maxDensity/tic)];
			loadData(res, avgValuesTest);			
			File resTarget = new File(Constants.RESOURCE_PATH+"/funDiag_2dir.csv");
			compareValues(avgValuesTest, resTarget);
     	}catch(Exception e){
     		e.printStackTrace();
     	}
	}
	
	@After
	public void deleteFolder(){
		deleteDirectory(new File(Constants.FD_TEST_PATH));
	}

	private static void compareValues(double[] avgValuesTest, File resTarget)
			throws FileNotFoundException, IOException {
		int[] countValues;
		BufferedReader br;
		String line;
		br = new BufferedReader(new FileReader(resTarget));
		br.readLine();		//first line contains labels
		line = br.readLine();
		double [] avgValuesTarget = new double[(int)(maxDensity/tic)];
		countValues = new int[avgValuesTarget.length];
		for(int i=0;i<avgValuesTarget.length;i++){
			avgValuesTarget[i]=0;
			countValues[i]=0;
		}
		boolean end = false;
		while (!end && line!=null){
			StringTokenizer st = new StringTokenizer(line,"\t");
			double density = Double.parseDouble(st.nextToken());
			double travelTime = Double.parseDouble(st.nextToken());
			if (density<=maxDensity){
				double flow = (10/travelTime)*density;
				int index = (int)(density/tic) - 1;
				avgValuesTarget[index]+=flow;
				countValues[index]++;
				line = br.readLine();				
			}else{
				end = true;
			}
		}
		br.close();
		for(int i=0;i<avgValuesTarget.length;i++){
			if (avgValuesTest[i]>0){
				avgValuesTarget[i]/=countValues[i];
				assertThat(avgValuesTest[i],closeTo(avgValuesTarget[i],acceptedError));
			}
		}
	}

	private static void loadData(File res, double[] avgValuesTest) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(res));
		br.readLine();		//first line describes the labels
		String line = br.readLine();
		
		int [] countValues = new int[avgValuesTest.length];
		for(int i=0;i<avgValuesTest.length;i++){
			avgValuesTest[i]=0;
			countValues[i]=0;
		}
		
		while (line!=null){
			StringTokenizer st = new StringTokenizer(line,",");
			double density = Double.parseDouble(st.nextToken());
			double travelTime = Double.parseDouble(st.nextToken());
			double flow = (scenarioLength/travelTime)*density;
			int index = (int)(density/tic) - 1;
			avgValuesTest[index]+=flow;
			countValues[index]++;
			line = br.readLine();	
		}
		br.close();
		for(int i=0;i<avgValuesTest.length;i++){
			avgValuesTest[i]/=countValues[i];
		}
	}
}

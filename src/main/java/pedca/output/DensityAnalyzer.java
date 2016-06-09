package pedca.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import matsimconnector.events.CAAgentChangeLinkEvent;
import matsimconnector.events.CAAgentConstructEvent;
import matsimconnector.events.CAAgentEnterEnvironmentEvent;
import matsimconnector.events.CAAgentExitEvent;
import matsimconnector.events.CAAgentLeaveEnvironmentEvent;
import matsimconnector.events.CAAgentMoveEvent;
import matsimconnector.events.CAAgentMoveToOrigin;
import matsimconnector.events.CAEngineStepPerformedEvent;
import matsimconnector.events.CAEventHandler;
import matsimconnector.utility.Constants;
import pedca.context.Context;
import pedca.environment.grid.DensityGrid;
import pedca.environment.grid.GridPoint;

public class DensityAnalyzer implements CAEventHandler{
	
	private String pathName;
	private float[][] cumulativeDensity;
	private DensityGrid densityGrid;
	private int pedsInside;
	private int stepsPerformed;
	private int timeWindowSize;
	
	public DensityAnalyzer(String pathName, Context contextCA){
		this.pathName = pathName+"/Density";
		this.densityGrid = contextCA.getDensityGrid();
		this.cumulativeDensity = new float[densityGrid.getRows()][densityGrid.getColumns()];
		this.timeWindowSize = 15;
		File path = new File(this.pathName);
		if(!path.exists())
			new File(this.pathName).mkdir();	
	}
	
	@Override
	public void reset(int iteration) {
	}

	@Override
	public void handleEvent(CAAgentConstructEvent event) {
	}

	@Override
	public void handleEvent(CAAgentMoveEvent event) {
	}

	@Override
	public void handleEvent(CAAgentExitEvent event) {
	}


	@Override
	public void handleEvent(CAAgentMoveToOrigin event) {
	}

	@Override
	public void handleEvent(CAAgentEnterEnvironmentEvent event) {
		this.pedsInside++;
	}	

	@Override
	public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
		this.pedsInside--;
	}


	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
	}


	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		if(pedsInside>0){
			for (int y=0;y<cumulativeDensity.length;y++)
				for(int x=0;x<cumulativeDensity[y].length;x++)
					cumulativeDensity[y][x] += densityGrid.getDensityAt(new GridPoint(x,y));
			stepsPerformed++;
			
			if (stepsPerformed==(int)(timeWindowSize/Constants.CA_STEP_DURATION)){
				try{
					File csvFile = new File(pathName+"/MeanDensity_"+(int)event.getTime()+".csv");
					if(csvFile.exists())
						csvFile.delete();			
					csvFile.createNewFile();
					FileWriter csvWriter;
					csvWriter = new FileWriter(csvFile);
					for (int y=cumulativeDensity.length-1;y>=0;y--){
						for(int x=0;x<cumulativeDensity[y].length;x++){
							csvWriter.write(cumulativeDensity[y][x]/stepsPerformed+",");
							cumulativeDensity[y][x]=0;
						}
						csvWriter.write("\n");
					}
					csvWriter.close();
				}catch (IOException e) {
					e.printStackTrace();
				}					
				stepsPerformed=0;
			}
		}
	}
}
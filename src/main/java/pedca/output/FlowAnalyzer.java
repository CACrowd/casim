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

public class FlowAnalyzer implements CAEventHandler{
	
	private File csvFile;
	private int totalFlowPerStep;
	private int lastSec;
	
	public FlowAnalyzer(String pathName){
		this.totalFlowPerStep = 0;
		this.lastSec = 0;
		try {
			 csvFile = new File(pathName+"/flow_data.csv");
			 FileWriter csvWriter;
			 if(!csvFile.exists()){
				new File(pathName).mkdir();
			    csvFile.createNewFile();
			    csvWriter = new FileWriter(csvFile);
			    csvWriter.write("Time[s],Flow[1/s]\n");
			    
			}else
				csvWriter = new FileWriter(csvFile,true);
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub
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
	}	

	@Override
	public void handleEvent(CAAgentLeaveEnvironmentEvent event) {
		totalFlowPerStep+=1;
	}


	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
	}


	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		if ((int)event.getTime()>lastSec){
			try {
				FileWriter csvWriter = new FileWriter(csvFile,true);
				csvWriter.write(event.getTime()+","+totalFlowPerStep+"\n");
				csvWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				totalFlowPerStep = 0;
				lastSec = (int)event.getTime();
			}
		}
	}

}

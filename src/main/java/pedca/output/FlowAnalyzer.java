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
	private File csvFileGlbAvg;
	private int totalFlowPerSec;
	private int lastSec;
	private float avgFlowForSteadyState;
	private int steadyStateBegin;
	private int steadyStateEnd;
	//private int timeWindowSize;
	//private float totalFlowPerTimeWindow;
	
	public FlowAnalyzer(String pathName){
		steadyStateBegin = 35;
		steadyStateEnd = 45;
		try {
			File path = new File(pathName);
			if(!path.exists())
				new File(pathName).mkdir();			
			
			csvFile = new File(pathName+"/flowData.csv");
			if(csvFile.exists())
				csvFile.delete();			
			csvFile.createNewFile();
			FileWriter csvWriter;
			csvWriter = new FileWriter(csvFile);
			csvWriter.write("#Time[s],Flow[1/s]\n");
			csvWriter.close();
			
			csvFileGlbAvg = new File(pathName+"/flowData_glbAvg.csv");
			if(csvFileGlbAvg.exists())
				csvFileGlbAvg.delete();			
			csvFileGlbAvg.createNewFile();
			csvWriter = new FileWriter(csvFileGlbAvg);
			csvWriter.write("#Time[s],Flow[1/s]\n");
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
		totalFlowPerSec+=1;
		int eventTime = (int)event.getTime();
		if (eventTime>steadyStateBegin && eventTime<steadyStateEnd)
			avgFlowForSteadyState+=1;
		//totalFlowPerTimeWindow+=1;
	}


	@Override
	public void handleEvent(CAAgentChangeLinkEvent event) {
	}


	@Override
	public void handleEvent(CAEngineStepPerformedEvent event) {
		int eventSec = (int)event.getTime();
		if (eventSec>lastSec){
			try {
				FileWriter csvWriter = new FileWriter(csvFile,true);
				csvWriter.write(eventSec+","+totalFlowPerSec+"\n");
				csvWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				totalFlowPerSec = 0;
				lastSec = eventSec;
			}
			if (eventSec == steadyStateEnd){
				try {
					FileWriter csvWriter = new FileWriter(csvFileGlbAvg,true);
					avgFlowForSteadyState/=(steadyStateEnd-steadyStateBegin);
					csvWriter.write(eventSec+","+avgFlowForSteadyState+"\n");
					csvWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
//			//flow averaged over the time window
//			if (eventSec%timeWindowSize == 0){
//				try {
//					FileWriter csvWriter = new FileWriter(csvFileMobAvg,true);
//					csvWriter.write(eventSec+","+(totalFlowPerTimeWindow/timeWindowSize)+"\n");
//					csvWriter.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				} finally{
//					totalFlowPerTimeWindow = 0;
//				}
//			}
		}
	}
}

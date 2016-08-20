package org.cacrowd.casim.matsimconnector.scenariogenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.cacrowd.casim.matsimconnector.run.StatenIslandRunner;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.gbl.MatsimRandom;

public class StatenIslandPopulationGenerator {

	public static void createPopulation(Scenario sc) {
		Network network = sc.getNetwork();
		ArrayList <Link> initLinks = new ArrayList<Link>();
		ArrayList <Link> destinationLinks = new ArrayList<Link>();
		for (Node node : network.getNodes().values()){
			if (isOriginNode(node)){
				initLinks.add(node.getOutLinks().values().iterator().next());
			}
			if(isDestinationNode(node)){
				destinationLinks.add(node.getOutLinks().values().iterator().next());
			}
		}
				
		Population population = sc.getPopulation();
		population.getPersons().clear();
		PopulationFactory factory = population.getFactory();

		File fileSG = new File(Constants.RESOURCE_PATH+"/countingSG.csv");
		File fileWH = new File(Constants.RESOURCE_PATH+"/countingWH.csv");;
		
		ArrayList<Integer> avgPerTimeSG = new ArrayList<Integer> ();
		ArrayList<Integer> avgPerTimeWH = new ArrayList<Integer> ();
		BufferedReader br;
		String line;
		try {
			br = new BufferedReader(new FileReader(fileSG));
			line = br.readLine();
			StringTokenizer st = new StringTokenizer(line,",");
			while(st.hasMoreTokens())
				avgPerTimeSG.add(Integer.parseInt(st.nextToken()));
			br = new BufferedReader(new FileReader(fileWH));
			line = br.readLine();
			st = new StringTokenizer(line,",");
			while(st.hasMoreTokens())
				avgPerTimeWH.add(Integer.parseInt(st.nextToken()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		final float tic = 15;
		float sigma = 1.5f *60;
		
		Link link = initLinks.get(0);
//		for (int i = 0; i < avgPerTimeSG.size(); i++) {

			float timeWindowEnd = 8.5f*3600; //tic*(i+1)*60;
			int nPeds = 1300;	//avgPerTimeWH.get(i);
			if ((timeWindowEnd >= StatenIslandRunner.peakTime1Start && timeWindowEnd<=StatenIslandRunner.peakTime1End)||(timeWindowEnd >= StatenIslandRunner.peakTime2Start && timeWindowEnd<=StatenIslandRunner.peakTime2End)){		
				for(int n = 0; n < nPeds; n++){
					double departureTime = (timeWindowEnd-(tic/2))+(MatsimRandom.getRandom().nextGaussian()*sigma);
					if (departureTime < 0)
						departureTime = 0;
					Person pers = factory.createPerson(Id.create("p"+population.getPersons().size(),Person.class));
					Plan plan = factory.createPlan();
					pers.addPlan(plan);
					Activity act0;
					act0 = factory.createActivityFromLinkId("origin", link.getId());
					
					act0.setEndTime(departureTime);
					plan.addActivity(act0);
					Leg leg = factory.createLeg("car");
					leg.setDepartureTime(departureTime);
					plan.addLeg(leg);
					Activity act1 = factory.createActivityFromLinkId("destination", getDestinationLinkId(link,destinationLinks));
					plan.addActivity(act1);
					population.addPerson(pers);
				}
			}
//		}
		

		link = initLinks.get(1);
//		for (int i = 0; i < avgPerTimeWH.size(); i++) {
		
			timeWindowEnd = 9*3600; //tic*(i+1)*60;
//			nPeds = 1300;	//avgPerTimeWH.get(i);
			if ((timeWindowEnd >= StatenIslandRunner.peakTime1Start && timeWindowEnd<=StatenIslandRunner.peakTime1End)||(timeWindowEnd >= StatenIslandRunner.peakTime2Start && timeWindowEnd<=StatenIslandRunner.peakTime2End)){
				for(int n = 0; n < nPeds; n++){
					double departureTime = (timeWindowEnd-(tic/2))+(MatsimRandom.getRandom().nextGaussian()*sigma);
					if (departureTime < 0)
						departureTime = 0;
					Person pers = factory.createPerson(Id.create("p"+population.getPersons().size(),Person.class));
					Plan plan = factory.createPlan();
					pers.addPlan(plan);
					Activity act0;
					act0 = factory.createActivityFromLinkId("origin", link.getId());
					
					act0.setEndTime(departureTime);
					plan.addActivity(act0);
					Leg leg = factory.createLeg("car");
					leg.setDepartureTime(departureTime);
					plan.addLeg(leg);
					Activity act1 = factory.createActivityFromLinkId("destination", getDestinationLinkId(link,destinationLinks));
					plan.addActivity(act1);
					population.addPerson(pers);
				}
			}
//		}
		
		
		
//		double t = 0;
//		double flowProportion = 1./initLinks.size();
//		int generated = 0;
//		for (Link link : initLinks){
//			int linkLimit = (int)(populationSize*flowProportion);
//			/*HOOGENDOORN EXP CONFIGURATION
//			linkLimit = 2000;
//			populationSize=4000;
//			String originNodeId = link.getFromNode().getId().toString();
//			if (originNodeId.endsWith("s")){
//				linkLimit = populationSize-linkLimit;
//				double cap = 5.*Constants.FLOPW_CAP_PER_METER_WIDTH;
//				link.setCapacity(cap);
//			}*/
//			for (int i = 0; i < linkLimit & generated<populationSize; i++) {
//				Person pers = factory.createPerson(Id.create("p"+population.getPersons().size(),Person.class));
//				Plan plan = factory.createPlan();
//				pers.addPlan(plan);
//				Activity act0;
//				act0 = factory.createActivityFromLinkId("origin", link.getId());
//				act0.setEndTime(t);
//				plan.addActivity(act0);
//				Leg leg = factory.createLeg("car");
//				plan.addLeg(leg);
//				Activity act1 = factory.createActivityFromLinkId("destination", getDestinationLinkId(link,destinationLinks));
//				plan.addActivity(act1);
//				population.addPerson(pers);
//				++generated;
//			}
//		}
	}

	private static Id<Link> getDestinationLinkId(Link originLink, ArrayList<Link> destinationLinks) {
		String originNodeId = originLink.getFromNode().getId().toString();
		if (originNodeId.endsWith("SG"))
			for (Link link : destinationLinks)
				if (link.getFromNode().getId().toString().endsWith("WH"))
					return link.getFromNode().getInLinks().values().iterator().next().getId();
		if (originNodeId.endsWith("WH"))
			for (Link link : destinationLinks)
				if (link.getFromNode().getId().toString().endsWith("SG"))
					return link.getFromNode().getInLinks().values().iterator().next().getId();
		return null;
	}

	private static boolean isOriginNode(Node node) {
		boolean result = false;
		for (int i=0;!result&&i<Constants.ORIGIN_FLOWS.length;i++){
			result=node.getId().toString().endsWith(""+Constants.ORIGIN_FLOWS[i]);
		}				
		return result;
		//return node.getId().toString().endsWith("n")||node.getId().toString().endsWith("s")||node.getId().toString().endsWith("w")||node.getId().toString().endsWith("e");
	}
	
	private static boolean isDestinationNode(Node node) {
		return node.getId().toString().endsWith("WH")||node.getId().toString().endsWith("SG");
	}

	
}

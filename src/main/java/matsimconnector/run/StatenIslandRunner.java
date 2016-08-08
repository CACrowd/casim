package matsimconnector.run;

import java.io.IOException;

import matsimconnector.congestionpricing.MSACongestionHandler;
import matsimconnector.congestionpricing.MSAMarginalCongestionPricingContolerListener;
import matsimconnector.congestionpricing.MSATollDisutilityCalculatorFactory;
import matsimconnector.congestionpricing.MSATollHandler;
import matsimconnector.engine.CAMobsimFactory;
import matsimconnector.engine.CATripRouterFactory;
import matsimconnector.scenario.CAScenario;
import matsimconnector.scenariogenerator.NetworkGenerator;
import matsimconnector.scenariogenerator.PopulationGenerator;
import matsimconnector.utility.Constants;
import matsimconnector.utility.IdUtility;
import matsimconnector.visualizer.debugger.eventsbaseddebugger.EventBasedVisDebuggerEngine;
import matsimconnector.visualizer.debugger.eventsbaseddebugger.InfoBox;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.ControlerConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.mobsim.framework.Mobsim;
import org.matsim.core.scenario.ScenarioUtils;

import pedca.context.Context;
import pedca.environment.network.Coordinate;
import scenarios.ContextGenerator;

import com.google.inject.Provider;

public class StatenIslandRunner implements IterationStartsListener {

	private static EventBasedVisDebuggerEngine dbg;
	private static String inputDir = Constants.INPUT_PATH;
	private static String outputDir = Constants.OUTPUT_PATH;
	private static int POPULATION_SIZE = 2000;
	private static String[] environmentFiles = {"stGeorge_1F_1.csv","WhiteHall_2F_4.csv"};
	private static double[] envRotation = {135, 45};
	

	public static void main(String[] args){
		Constants.SIMULATION_ITERATIONS = 30;
		Constants.SIMULATION_DURATION = 16000;
		Constants.VIS = true;
		Constants.ORIGIN_FLOWS[0] = "2n";
		generateScenario();
		runSimulation();
	}
	
	
	public static void generateScenario() {				
		Config c = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(c);
		
		Context[] contextCAs = new Context[environmentFiles.length];
		for (int i = 0;i<contextCAs.length;i++){		
			contextCAs[i] = ContextGenerator.createContextWithResourceEnvironmentFileV2(environmentFiles[i]);
			contextCAs[i].environmentOrigin = new Coordinate(i*150, i*200);
			contextCAs[i].environmentRotation = envRotation[i];
			try {
				contextCAs[i].saveConfiguration(inputDir+"/CAScenario/input"+i);
			} catch (IOException e) {
				e.printStackTrace();
			}
			NetworkGenerator.createNetwork(scenario, contextCAs[i]);
		}		
		
		c.network().setInputFile(inputDir + "/network.xml.gz");
		c.strategy().addParam("Module_1", "ReRoute");
		c.strategy().addParam("ModuleProbability_1", ".1");
		c.strategy().addParam("ModuleDisableAfterIteration_1", "10");
		c.strategy().addParam("Module_2", "ChangeExpBeta");
		c.strategy().addParam("ModuleProbability_2", ".9");
		c.strategy().addParam("Module_3", "ReRoute");
		c.strategy().addParam("ModuleProbability_3", ".05");
		c.strategy().addParam("ModuleDisableAfterIteration_3", "50");
		c.strategy().setMaxAgentPlanMemorySize(5);

		c.controler().setOutputDirectory(outputDir);
		c.controler().setLastIteration(0);
		c.controler().setRoutingAlgorithmType(ControlerConfigGroup.RoutingAlgorithmType.AStarLandmarks);

		c.plans().setInputFile(inputDir + "/population.xml.gz");

		ActivityParams pre = new ActivityParams("origin");
		// needs to be geq 49, otherwise when running a simulation one gets "java.lang.RuntimeException: zeroUtilityDuration of type pre-evac must be greater than 0.0. Did you forget to specify the typicalDuration?"
		// the reason is the double precision. see also comment in ActivityUtilityParameters.java (gl)
		pre.setTypicalDuration(49); 
		pre.setMinimalDuration(49);
		pre.setClosingTime(49);
		pre.setEarliestEndTime(49);
		pre.setLatestStartTime(49);
		pre.setOpeningTime(49);

		ActivityParams post = new ActivityParams("destination");
		post.setTypicalDuration(49); 
		post.setMinimalDuration(49);
		post.setClosingTime(49);
		post.setEarliestEndTime(49);
		post.setLatestStartTime(49);
		post.setOpeningTime(49);
		scenario.getConfig().planCalcScore().addActivityParams(pre);
		scenario.getConfig().planCalcScore().addActivityParams(post);
		scenario.getConfig().planCalcScore().setLateArrival_utils_hr(0.);
		scenario.getConfig().planCalcScore().setPerforming_utils_hr(0.);

		QSimConfigGroup qsim = scenario.getConfig().qsim();
		qsim.setEndTime(20*60);
		qsim.setStuckTime(100000);
		c.controler().setMobsim(Constants.CA_MOBSIM_MODE);
		c.global().setCoordinateSystem(Constants.COORDINATE_SYSTEM);
		c.qsim().setEndTime(60*10);

		c.travelTimeCalculator().setTraveltimeBinSize(60);
		c.planCalcScore().setBrainExpBeta(1);

		PopulationGenerator.createPopulation(scenario, POPULATION_SIZE);
		
		new ConfigWriter(c).write(inputDir+ "/config.xml");
		new NetworkWriter(scenario.getNetwork()).write(c.network().getInputFile());
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(c.plans().getInputFile());
	}
	
	@SuppressWarnings("deprecation")
	public static void runSimulation() {
		Config c = ConfigUtils.loadConfig(inputDir+"/config.xml");
		Scenario scenario = ScenarioUtils.loadScenario(c);
		CAScenario scenarioCA = new CAScenario(inputDir+"/CAScenario", environmentFiles.length);
		scenarioCA.initNetworks();
		//HybridNetworkBuilder.buildNetwork(scenarioCA.getCAEnvironment(Id.create("0", CAEnvironment.class)), scenarioCA);
		scenarioCA.connect(scenario);
		
		cleanNetwork(scenario.getNetwork());
		
		new NetworkWriter(scenario.getNetwork()).write(c.network().getInputFile());
		
//		System.exit(0);
		
		c.controler().setWriteEventsInterval(1);
		c.controler().setLastIteration(Constants.SIMULATION_ITERATIONS-1);
		c.qsim().setEndTime(Constants.SIMULATION_DURATION);

		final Controler controller = new Controler(scenario);
		final MSATollHandler tollHandler = new MSATollHandler(controller.getScenario());
		final MSATollDisutilityCalculatorFactory tollDisutilityCalculatorFactory = new MSATollDisutilityCalculatorFactory(tollHandler, c.planCalcScore());

		if (Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION) {
			controller.addOverridingModule(new AbstractModule(){
				@Override
				public void install() {
					this.bindCarTravelDisutilityFactory().toInstance(tollDisutilityCalculatorFactory);
				}
			}); 
			
			controller.addControlerListener(new MSAMarginalCongestionPricingContolerListener(controller.getScenario(), tollHandler, new MSACongestionHandler(controller.getEvents(), controller.getScenario())));
		}

		controller.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

		controller.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addRoutingModuleBinding(Constants.CAR_LINK_MODE).toProvider(CATripRouterFactory.class);
			}
		});
				
		final CAMobsimFactory factoryCA = new CAMobsimFactory();
		controller.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				if (getConfig().controler().getMobsim().equals(Constants.CA_MOBSIM_MODE)) {
					bind(Mobsim.class).toProvider(new Provider<Mobsim>() {
						@Override
						public Mobsim get() {
							return factoryCA.createMobsim(controller.getScenario(), controller.getEvents());
						}
					});
				}
			}
		});

		if (Constants.VIS) {
			dbg = new EventBasedVisDebuggerEngine(scenario);
			InfoBox iBox = new InfoBox(dbg, scenario);
			dbg.addAdditionalDrawer(iBox);
			controller.getEvents().addHandler(dbg);
		}
		
		StatenIslandRunner runner = new StatenIslandRunner();
		controller.addControlerListener(runner);
		controller.run();
	}

	private static void cleanNetwork(Network net) {
		net.removeLink(IdUtility.createLinkId(0, 19, 23));
		net.removeLink(IdUtility.createLinkId(0, 23, 19));
		net.removeLink(IdUtility.createLinkId(0, 24, 22));
		net.removeLink(IdUtility.createLinkId(0, 25, 23));
		
		net.removeLink(IdUtility.createLinkId(0, 16, 26));
		net.removeLink(IdUtility.createLinkId(0, 18, 26));
		net.removeLink(IdUtility.createLinkId(0, 19, 26));
		net.removeLink(IdUtility.createLinkId(0, 20, 26));
		
		net.removeLink(IdUtility.createLinkId(0, 16, 18));
		net.removeLink(IdUtility.createLinkId(0, 16, 19));
		net.removeLink(IdUtility.createLinkId(0, 16, 20));
		net.removeLink(IdUtility.createLinkId(0, 18, 16));
		net.removeLink(IdUtility.createLinkId(0, 18, 20));
		net.removeLink(IdUtility.createLinkId(0, 18, 19));
		net.removeLink(IdUtility.createLinkId(0, 18, 22));
		net.removeLink(IdUtility.createLinkId(0, 19, 16));
		net.removeLink(IdUtility.createLinkId(0, 19, 18));
		net.removeLink(IdUtility.createLinkId(0, 19, 20));
		net.removeLink(IdUtility.createLinkId(0, 20, 18));
		net.removeLink(IdUtility.createLinkId(0, 20, 19));
		net.removeLink(IdUtility.createLinkId(0, 20, 16));
		
		net.removeLink(IdUtility.createLinkId(0, 20, 22));
		net.removeLink(IdUtility.createLinkId(0, 22, 20));
		net.removeLink(IdUtility.createLinkId(0, 22, 18));
		net.removeLink(IdUtility.createLinkId(0, 22, 14));
		net.removeLink(IdUtility.createLinkId(0, 20, 14));
		net.removeLink(IdUtility.createLinkId(0, 24, 22));

		net.removeLink(IdUtility.createLinkId(1, 15, 22));
		net.removeLink(IdUtility.createLinkId(1, 16, 18));
		net.removeLink(IdUtility.createLinkId(1, 17, 18));
		net.removeLink(IdUtility.createLinkId(1, 22, 25));
		net.removeLink(IdUtility.createLinkId(1, 25, 22));
		net.removeLink(IdUtility.createLinkId(1, 27, 30));
		net.removeLink(IdUtility.createLinkId(1, 28, 30));
		net.removeLink(IdUtility.createLinkId(1, 28, 25));
		net.removeLink(IdUtility.createLinkId(1, 29, 26));
		net.removeLink(IdUtility.createLinkId(1, 29, 30));
		
	}


	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if (dbg != null)
			dbg.startIteration(event.getIteration()); 
	}

}

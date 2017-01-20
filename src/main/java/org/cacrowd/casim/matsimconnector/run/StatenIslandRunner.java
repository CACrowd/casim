/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.matsimconnector.run;

import java.io.IOException;

import org.cacrowd.casim.matsimconnector.congestionpricing.MSACongestionHandler;
import org.cacrowd.casim.matsimconnector.congestionpricing.MSAMarginalCongestionPricingContolerListener;
import org.cacrowd.casim.matsimconnector.congestionpricing.MSATollDisutilityCalculatorFactory;
import org.cacrowd.casim.matsimconnector.congestionpricing.MSATollHandler;
import org.cacrowd.casim.matsimconnector.engine.CAMobsimFactory;
import org.cacrowd.casim.matsimconnector.engine.CATripRouterFactory;
import org.cacrowd.casim.matsimconnector.scenario.CAScenario;
import org.cacrowd.casim.matsimconnector.scenariogenerator.StatenIslandNetworkGenerator;
import org.cacrowd.casim.matsimconnector.scenariogenerator.StatenIslandPopulationGenerator;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.utility.IdUtility;
import org.cacrowd.casim.matsimconnector.utility.MathUtility;
import org.cacrowd.casim.matsimconnector.visualizer.debugger.eventsbaseddebugger.EventBasedVisDebuggerEngine;
import org.cacrowd.casim.matsimconnector.visualizer.debugger.eventsbaseddebugger.InfoBox;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.network.Coordinate;
import org.cacrowd.casim.scenarios.ContextGenerator;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
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

import com.google.inject.Provider;

public class StatenIslandRunner implements IterationStartsListener {
	private static EventBasedVisDebuggerEngine dbg;
	private static String inputDir = Constants.INPUT_PATH;
	private static String outputDir = Constants.OUTPUT_PATH;
	private static String[] environmentFiles = {"stGeorge_1F_4.csv","WhiteHall_2Fv3.csv", "WhiteHall_GF3.csv" };
	private static double[] envRotation = { 120, 10, 10 };

	public static final int peakTime1Start = (int) (6.5 * 3600);
	public static final int peakTime1End = (int) (10 * 3600);
	public static final int peakTime2Start = (int) (15 * 3600);
	public static final int peakTime2End = (int) (19 * 3600);

	private static int scenario = 1;
	static {
		if (scenario == 2)
			environmentFiles[1] = "WhiteHall_2Fv2.csv";
	}

	public static void main(String[] args) {
		Constants.SIMULATION_ITERATIONS = 30;
		Constants.SIMULATION_DURATION = 23.9 * 3600;
		Constants.VIS = true;
		Constants.SAVE_FRAMES = false;
		String[] origins = { "SG", "WH" };
		Constants.ORIGIN_FLOWS = origins;
		generateScenario();
		runSimulation();
	}

	public static void generateScenario() {
		Config c = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(c);

		Context[] contextCAs = new Context[environmentFiles.length];
		for (int i = 0; i < contextCAs.length; i++) {
			contextCAs[i] = ContextGenerator
					.createContextWithResourceEnvironmentFileV2(
							environmentFiles[i], i);
			Coordinate point;
			if (i == 0)
				point = new Coordinate(0, 0);
			else {
				point = new Coordinate(0, 8000);
				MathUtility.rotate(point, -38 + 2.5 * (i - 1));
			}
			contextCAs[i].environmentOrigin = point;
			contextCAs[i].environmentRotation = envRotation[i];
			try {
				contextCAs[i].saveConfiguration(inputDir + "/CAScenario/input"+ i);
			} catch (IOException e) {
				e.printStackTrace();
			}
			StatenIslandNetworkGenerator.createNetwork(scenario, contextCAs[i]);
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
		c.controler().setRoutingAlgorithmType(
				ControlerConfigGroup.RoutingAlgorithmType.AStarLandmarks);

		c.plans().setInputFile(inputDir + "/population.xml.gz");

		ActivityParams pre = new ActivityParams("origin");
		// needs to be geq 49, otherwise when running a simulation one gets
		// "java.lang.RuntimeException: zeroUtilityDuration of type pre-evac must be greater than 0.0. Did you forget to specify the typicalDuration?"
		// the reason is the double precision. see also comment in
		// ActivityUtilityParameters.java (gl)
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
		qsim.setEndTime(23.9 * 3600);
		qsim.setStuckTime(10000000);
		c.controler().setMobsim(Constants.CA_MOBSIM_MODE);
		c.global().setCoordinateSystem(Constants.COORDINATE_SYSTEM);
		c.qsim().setEndTime(60 * 10);

		c.travelTimeCalculator().setTraveltimeBinSize(60);
		c.planCalcScore().setBrainExpBeta(1);

		StatenIslandPopulationGenerator.createPopulation(scenario);

		new ConfigWriter(c).write(inputDir + "/config.xml");
		new NetworkWriter(scenario.getNetwork()).write(c.network().getInputFile());
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(c.plans().getInputFile());
	}

	@SuppressWarnings("deprecation")
	public static void runSimulation() {
		Config c = ConfigUtils.loadConfig(inputDir + "/config.xml");
//		c.plans().setInputFile("C:/Users/Luca/Documents/uni/Dottorato/Juelich/developing_stuff/Test/debug/SI_SeptRes/futureWH_2boardingFs/output_plans.xml.gz");
		c.plans().setInputFile("C:/Users/Luca/Documents/uni/Dottorato/Juelich/developing_stuff/Test/debug/SI_november/WH_FP_video/output_plans.xml.gz");
		
		Scenario scenario = ScenarioUtils.loadScenario(c);
		CAScenario scenarioCA = new CAScenario(inputDir + "/CAScenario",environmentFiles.length);
		scenarioCA.initNetworks();
		// HybridNetworkBuilder.buildNetwork(scenarioCA.getCAEnvironment(Id.create("0", CAEnvironment.class)), scenarioCA);
		scenarioCA.connect(scenario);

		cleanNetwork(scenario.getNetwork());

		new NetworkWriter(scenario.getNetwork()).write(c.network().getInputFile());

		// System.exit(0);

		c.controler().setWriteEventsInterval(1);
		c.controler().setLastIteration(Constants.SIMULATION_ITERATIONS - 1);
		c.qsim().setEndTime(Constants.SIMULATION_DURATION);

		final Controler controller = new Controler(scenario);
		final MSATollHandler tollHandler = new MSATollHandler(
				controller.getScenario());
		final MSATollDisutilityCalculatorFactory tollDisutilityCalculatorFactory = new MSATollDisutilityCalculatorFactory(
				tollHandler, c.planCalcScore());

		if (Constants.MARGINAL_SOCIAL_COST_OPTIMIZATION) {
			controller.addOverridingModule(new AbstractModule() {
				@Override
				public void install() {
					this.bindCarTravelDisutilityFactory().toInstance(
							tollDisutilityCalculatorFactory);
				}
			});

			controller
					.addControlerListener(new MSAMarginalCongestionPricingContolerListener(
							controller.getScenario(), tollHandler,
							new MSACongestionHandler(controller.getEvents(),
									controller.getScenario())));
		}

		controller
				.getConfig()
				.controler()
				.setOverwriteFileSetting(
						OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);

		controller.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				addRoutingModuleBinding(Constants.CAR_LINK_MODE).toProvider(
						CATripRouterFactory.class);
			}
		});

		final CAMobsimFactory factoryCA = new CAMobsimFactory();
		controller.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				if (getConfig().controler().getMobsim()
						.equals(Constants.CA_MOBSIM_MODE)) {
					bind(Mobsim.class).toProvider(new Provider<Mobsim>() {
						@Override
						public Mobsim get() {
							return factoryCA.createMobsim(
									controller.getScenario(),
									controller.getEvents());
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

		// controller.getEvents().addHandler(new TravelTimeAnalyzer(outputDir));
		// controller.getEvents().addHandler(new CANodeFlowAnalyzer(outputDir));
		StatenIslandRunner runner = new StatenIslandRunner();
		controller.addControlerListener(runner);
		controller.run();
	}

	private static void cleanNetwork(Network net) {
		net.removeLink(IdUtility.createLinkId(0, 10, 1));
		net.removeLink(IdUtility.createLinkId(0, 1, 10));
		net.removeLink(IdUtility.createLinkId(0, 11, 0));
		net.removeLink(IdUtility.createLinkId(0, 0, 11));
		net.removeLink(IdUtility.createLinkId(0, 0, 1));
		net.removeLink(IdUtility.createLinkId(0, 1, 0));

		net.removeLink(IdUtility.createLinkId(0, 15, 23));
		net.removeLink(IdUtility.createLinkId(0, 14, 23));
		net.removeLink(IdUtility.createLinkId(0, 15, 16));
		net.removeLink(IdUtility.createLinkId(0, 14, 16));

		net.removeLink(IdUtility.createLinkId(0, 16, 30));
		net.removeLink(IdUtility.createLinkId(0, 16, 24));
		net.removeLink(IdUtility.createLinkId(0, 17, 30));
		net.removeLink(IdUtility.createLinkId(0, 18, 30));
		net.removeLink(IdUtility.createLinkId(0, 19, 30));
		net.removeLink(IdUtility.createLinkId(0, 20, 30));
		net.removeLink(IdUtility.createLinkId(0, 21, 30));
		net.removeLink(IdUtility.createLinkId(0, 24, 26));
		net.removeLink(IdUtility.createLinkId(0, 24, 16));
		net.removeLink(IdUtility.createLinkId(0, 33, 24));
		net.removeLink(IdUtility.createLinkId(0, 33, 26));
		net.removeLink(IdUtility.createLinkId(0, 35, 26));

		net.removeLink(IdUtility.createLinkId(0, 23, 25));
		net.removeLink(IdUtility.createLinkId(0, 23, 26));
		net.removeLink(IdUtility.createLinkId(0, 23, 24));
		net.removeLink(IdUtility.createLinkId(0, 23, 30));
		net.removeLink(IdUtility.createLinkId(0, 24, 23));
		net.removeLink(IdUtility.createLinkId(0, 25, 23));
		net.removeLink(IdUtility.createLinkId(0, 25, 26));
		net.removeLink(IdUtility.createLinkId(0, 25, 30));
		net.removeLink(IdUtility.createLinkId(0, 26, 25));
		net.removeLink(IdUtility.createLinkId(0, 26, 23));
		net.removeLink(IdUtility.createLinkId(0, 26, 30));
		net.removeLink(IdUtility.createLinkId(0, 26, 35));
		net.removeLink(IdUtility.createLinkId(0, 30, 25));
		net.removeLink(IdUtility.createLinkId(0, 30, 26));
		net.removeLink(IdUtility.createLinkId(0, 30, 23));

		net.removeLink(IdUtility.createLinkId(0, 25, 30));
		net.removeLink(IdUtility.createLinkId(0, 26, 30));
		net.removeLink(IdUtility.createLinkId(0, 37, 40));
		net.removeLink(IdUtility.createLinkId(0, 38, 35));
		net.removeLink(IdUtility.createLinkId(0, 39, 36));

		net.removeLink(IdUtility.createLinkId(0, 10, 11));
		net.removeLink(IdUtility.createLinkId(0, 11, 10));
		net.removeLink(IdUtility.createLinkId(0, 10, 15));
		net.removeLink(IdUtility.createLinkId(0, 15, 10));

		net.removeLink(IdUtility.createLinkId(0, 11, 14));
		net.removeLink(IdUtility.createLinkId(0, 14, 11));
		
		
		net.removeLink(IdUtility.createLinkId(0, 14, 15));
		net.removeLink(IdUtility.createLinkId(0, 15, 14));

		net.removeLink(IdUtility.createLinkId(0, 36, 39)); // West exit corridor
		net.removeLink(IdUtility.createLinkId(0, 37, 30)); // Western boarding door
//		net.removeLink(IdUtility.createLinkId(0, 37, 26)); // Eastern boarding door
//		net.removeLink(IdUtility.createLinkId(0, 37, 25)); // Mid West boarding door
		
		net.removeLink(IdUtility.createLinkId(0, 23, 16));
		net.removeLink(IdUtility.createLinkId(0, 16, 23));
		
		net.removeLink(IdUtility.createLinkId(0, 24, 33));
		net.removeLink(IdUtility.createLinkId(0, 24, 14));
		net.removeLink(IdUtility.createLinkId(0, 23, 15));
		net.removeLink(IdUtility.createLinkId(0, 16, 15));
		
		//regarding flow from Mid West boarding door
		net.removeLink(IdUtility.createLinkId(0, 16, 25));
		net.removeLink(IdUtility.createLinkId(0, 25, 17));
		net.removeLink(IdUtility.createLinkId(0, 17, 25));
		net.removeLink(IdUtility.createLinkId(0, 25, 18));
		net.removeLink(IdUtility.createLinkId(0, 18, 25));
		net.removeLink(IdUtility.createLinkId(0, 25, 19));
		net.removeLink(IdUtility.createLinkId(0, 19, 25));
		net.removeLink(IdUtility.createLinkId(0, 25, 20));
		net.removeLink(IdUtility.createLinkId(0, 20, 25));
		net.removeLink(IdUtility.createLinkId(0, 25, 21));
		net.removeLink(IdUtility.createLinkId(0, 21, 25));

		if (scenario == 1) {
			//Whitehall 1F
			net.removeLink(IdUtility.createLinkId(1, 14, 15));
			net.removeLink(IdUtility.createLinkId(1, 15, 14));
			net.removeLink(IdUtility.createLinkId(1, 24, 25));
			net.removeLink(IdUtility.createLinkId(1, 25, 24));

			net.removeLink(IdUtility.createLinkId(1, 43, 46));
			net.removeLink(IdUtility.createLinkId(1, 44, 41));
			net.removeLink(IdUtility.createLinkId(1, 45, 42));

			net.removeLink(IdUtility.createLinkId(1, 34, 35));
			net.removeLink(IdUtility.createLinkId(1, 34, 39));
			net.removeLink(IdUtility.createLinkId(1, 34, 40));
			net.removeLink(IdUtility.createLinkId(1, 35, 34));
			net.removeLink(IdUtility.createLinkId(1, 35, 39));
			net.removeLink(IdUtility.createLinkId(1, 35, 40));
			net.removeLink(IdUtility.createLinkId(1, 39, 35));
			net.removeLink(IdUtility.createLinkId(1, 39, 34));
			net.removeLink(IdUtility.createLinkId(1, 39, 40));
			net.removeLink(IdUtility.createLinkId(1, 40, 35));
			net.removeLink(IdUtility.createLinkId(1, 40, 39));
			net.removeLink(IdUtility.createLinkId(1, 40, 34));

			net.removeLink(IdUtility.createLinkId(1, 42, 45)); // East exit
																// corridor
			// net.removeLink(IdUtility.createLinkId(1, 43, 38)); //W boarding
			// door
			net.removeLink(IdUtility.createLinkId(1, 43, 33)); // access to ME boarding doors
			net.removeLink(IdUtility.createLinkId(1, 38, 33)); // access to ME boarding doors (avoiding 43 -> 38 -> 33)
			net.removeLink(IdUtility.createLinkId(1, 33, 35)); // ME boarding door
			net.removeLink(IdUtility.createLinkId(1, 33, 39)); // ME boarding door
			net.removeLink(IdUtility.createLinkId(1, 33, 40)); // ME boarding door

			net.removeLink(IdUtility.createLinkId(1, 29, 38));
			net.removeLink(IdUtility.createLinkId(1, 29, 30));
			net.removeLink(IdUtility.createLinkId(1, 29, 31));
			net.removeLink(IdUtility.createLinkId(1, 30, 31));
			net.removeLink(IdUtility.createLinkId(1, 30, 29));
			net.removeLink(IdUtility.createLinkId(1, 30, 38));
			net.removeLink(IdUtility.createLinkId(1, 31, 38));
			// net.removeLink(IdUtility.createLinkId(1, 31, 30));
			net.removeLink(IdUtility.createLinkId(1, 31, 29));
			net.removeLink(IdUtility.createLinkId(1, 31, 38));
			net.removeLink(IdUtility.createLinkId(1, 33, 38));
			net.removeLink(IdUtility.createLinkId(1, 38, 41));
			net.removeLink(IdUtility.createLinkId(1, 41, 38));

			net.removeLink(IdUtility.createLinkId(1, 1, 14));
			net.removeLink(IdUtility.createLinkId(1, 14, 1));
			net.removeLink(IdUtility.createLinkId(1, 1, 0));
			net.removeLink(IdUtility.createLinkId(1, 0, 1));
			net.removeLink(IdUtility.createLinkId(1, 0, 15));
			net.removeLink(IdUtility.createLinkId(1, 15, 0));

			// WHITEHALL GROUND FLOOR
			//v3
			net.removeLink(IdUtility.createLinkId(2, 5, 3));
			net.removeLink(IdUtility.createLinkId(2, 5, 7));
			net.removeLink(IdUtility.createLinkId(2, 7, 5));
			net.removeLink(IdUtility.createLinkId(2, 7, 9));
			net.removeLink(IdUtility.createLinkId(2, 8, 6));
			net.removeLink(IdUtility.createLinkId(2, 8, 9));
			net.removeLink(IdUtility.createLinkId(2, 9, 8));
			net.removeLink(IdUtility.createLinkId(2, 10, 11));
			net.removeLink(IdUtility.createLinkId(2, 11, 10));
			
			
//			net.removeLink(Id.create("l95", Link.class));
//			net.removeLink(IdUtility.createLinkId(2, 14, 12));
//			net.removeLink(IdUtility.createLinkId(2, 13, 15));
//			net.removeLink(IdUtility.createLinkId(2, 11, 13));
//			net.removeLink(IdUtility.createLinkId(2, 13, 11));
			
			//with v2
//			net.removeLink(IdUtility.createLinkId(2, 16, 13));
//			net.removeLink(IdUtility.createLinkId(2, 14, 16));
//			net.removeLink(IdUtility.createLinkId(2, 12, 14));
//			net.removeLink(IdUtility.createLinkId(2, 14, 12));
			
//			net.removeLink(IdUtility.createLinkId(2, 0, 1));
//			net.removeLink(IdUtility.createLinkId(2, 0, 4));
//			net.removeLink(IdUtility.createLinkId(2, 1, 0));
//			net.removeLink(IdUtility.createLinkId(2, 1, 3));
//			net.removeLink(IdUtility.createLinkId(2, 3, 1));
//			net.removeLink(IdUtility.createLinkId(2, 3, 4));
//			net.removeLink(IdUtility.createLinkId(2, 4, 0));
//			net.removeLink(IdUtility.createLinkId(2, 4, 3));
//			
//			//with v2
//			net.removeLink(IdUtility.createLinkId(2, 7, 8));
//			net.removeLink(IdUtility.createLinkId(2, 8, 7));
//			net.removeLink(IdUtility.createLinkId(2, 9, 10));
//			net.removeLink(IdUtility.createLinkId(2, 10, 9));
//			net.removeLink(IdUtility.createLinkId(2, 8, 9));
//			net.removeLink(IdUtility.createLinkId(2, 9, 8));
//			net.removeLink(IdUtility.createLinkId(2, 7, 10));
//			net.removeLink(IdUtility.createLinkId(2, 10, 7));
//			
//			net.removeLink(IdUtility.createLinkId(2, 10, 15));
//			net.removeLink(IdUtility.createLinkId(2, 9, 14));
//			net.removeLink(IdUtility.createLinkId(2, 14, 15));
//			net.removeLink(IdUtility.createLinkId(2, 15, 14));
//			net.removeLink(IdUtility.createLinkId(2, 15, 12));
//			net.removeLink(IdUtility.createLinkId(2, 12, 15));
//			net.removeLink(IdUtility.createLinkId(2, 12, 9));
//			net.removeLink(IdUtility.createLinkId(2, 12, 10));
//			net.removeLink(IdUtility.createLinkId(2, 15, 17));
//			net.removeLink(IdUtility.createLinkId(2, 15, 9));
//			net.removeLink(IdUtility.createLinkId(2, 14, 10));
//			net.removeLink(IdUtility.createLinkId(2, 10, 14));
//			net.removeLink(IdUtility.createLinkId(2, 9, 15));
//			net.removeLink(IdUtility.createLinkId(2, 17, 15));
//			net.removeLink(IdUtility.createLinkId(2, 14, 18));
//			net.removeLink(IdUtility.createLinkId(2, 18, 14));
//			net.removeLink(IdUtility.createLinkId(2, 17, 18));
//			net.removeLink(IdUtility.createLinkId(2, 18, 17));
//			net.removeLink(IdUtility.createLinkId(2, 14, 17));
//			net.removeLink(IdUtility.createLinkId(2, 15, 18));
//			
//			net.removeLink(IdUtility.createLinkId(2, 16, 17));
//			net.removeLink(IdUtility.createLinkId(2, 17, 18));
//			net.removeLink(IdUtility.createLinkId(2, 18, 17));
//			net.removeLink(IdUtility.createLinkId(2, 16, 18));
//			net.removeLink(IdUtility.createLinkId(2, 17, 16));
//			net.removeLink(IdUtility.createLinkId(2, 18, 16));
			

		} else if (scenario == 2) {

			net.removeLink(IdUtility.createLinkId(1, 14, 15));
			net.removeLink(IdUtility.createLinkId(1, 15, 14));
			net.removeLink(IdUtility.createLinkId(1, 24, 25));
			net.removeLink(IdUtility.createLinkId(1, 25, 24));

			net.removeLink(IdUtility.createLinkId(1, 43, 46));
			net.removeLink(IdUtility.createLinkId(1, 44, 41));
			net.removeLink(IdUtility.createLinkId(1, 45, 42));

			net.removeLink(IdUtility.createLinkId(1, 34, 35));
			net.removeLink(IdUtility.createLinkId(1, 34, 39));
			net.removeLink(IdUtility.createLinkId(1, 34, 40));
			net.removeLink(IdUtility.createLinkId(1, 35, 34));
			net.removeLink(IdUtility.createLinkId(1, 35, 39));
			net.removeLink(IdUtility.createLinkId(1, 35, 40));
			net.removeLink(IdUtility.createLinkId(1, 39, 34));
			net.removeLink(IdUtility.createLinkId(1, 39, 40));
			net.removeLink(IdUtility.createLinkId(1, 40, 35));
			net.removeLink(IdUtility.createLinkId(1, 40, 39));
			net.removeLink(IdUtility.createLinkId(1, 40, 34));

			net.removeLink(IdUtility.createLinkId(1, 42, 45)); // East exit corridor

			net.removeLink(IdUtility.createLinkId(1, 43, 38)); // West boarding door
			net.removeLink(IdUtility.createLinkId(1, 33, 38)); // West boarding door

			net.removeLink(IdUtility.createLinkId(1, 33, 35)); // ME boarding door

			// net.removeLink(IdUtility.createLinkId(1, 33, 39)); //ME boarding door
			net.removeLink(IdUtility.createLinkId(1, 33, 40)); // ME boarding door

			net.removeLink(IdUtility.createLinkId(1, 29, 38));
			net.removeLink(IdUtility.createLinkId(1, 29, 30));
			net.removeLink(IdUtility.createLinkId(1, 29, 31));
			net.removeLink(IdUtility.createLinkId(1, 30, 31));
			net.removeLink(IdUtility.createLinkId(1, 30, 29));
			net.removeLink(IdUtility.createLinkId(1, 30, 38));
			net.removeLink(IdUtility.createLinkId(1, 31, 38));
			net.removeLink(IdUtility.createLinkId(1, 31, 30));
			net.removeLink(IdUtility.createLinkId(1, 31, 29));
			net.removeLink(IdUtility.createLinkId(1, 41, 38));
			net.removeLink(IdUtility.createLinkId(1, 38, 41));

			net.removeLink(IdUtility.createLinkId(1, 1, 14));
			net.removeLink(IdUtility.createLinkId(1, 14, 1));
			net.removeLink(IdUtility.createLinkId(1, 1, 0));
			net.removeLink(IdUtility.createLinkId(1, 0, 1));
			net.removeLink(IdUtility.createLinkId(1, 0, 15));
			net.removeLink(IdUtility.createLinkId(1, 15, 0));
		}

		// boarding ramps
		// net.removeLink(Id.create("l41",Link.class));
		// net.removeLink(Id.create("l40",Link.class));
		net.removeLink(Id.create("l44", Link.class));
		net.removeLink(Id.create("l45", Link.class));
		// net.removeLink(Id.create("l2",Link.class));
		// net.removeLink(Id.create("l3",Link.class));
		net.removeLink(Id.create("l4", Link.class));
		net.removeLink(Id.create("l5", Link.class));
		net.removeLink(Id.create("l6", Link.class));
		net.removeLink(Id.create("l7", Link.class));
		net.removeLink(Id.create("l82", Link.class));
		net.removeLink(Id.create("l78", Link.class));
		net.removeLink(Id.create("l80", Link.class));
		net.removeLink(Id.create("l84", Link.class));
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if (dbg != null)
			dbg.startIteration(event.getIteration());
	}

}
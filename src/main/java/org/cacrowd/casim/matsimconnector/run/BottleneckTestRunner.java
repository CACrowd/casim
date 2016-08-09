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

import com.google.inject.Provider;
import org.cacrowd.casim.matsimconnector.engine.CAMobsimFactory;
import org.cacrowd.casim.matsimconnector.engine.CATripRouterFactory;
import org.cacrowd.casim.matsimconnector.network.HybridNetworkBuilder;
import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.matsimconnector.scenario.CAScenario;
import org.cacrowd.casim.matsimconnector.scenariogenerator.NetworkGenerator;
import org.cacrowd.casim.matsimconnector.scenariogenerator.PopulationGenerator;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.visualizer.debugger.eventsbaseddebugger.EventBasedVisDebuggerEngine;
import org.cacrowd.casim.matsimconnector.visualizer.debugger.eventsbaseddebugger.InfoBox;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.output.DensityAnalyzer;
import org.cacrowd.casim.pedca.output.FlowAnalyzer;
import org.cacrowd.casim.scenarios.ContextGenerator;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
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

public class BottleneckTestRunner implements IterationStartsListener {

	private String inputDir;
	private String outputDir;
	private int pedsNumber;	
	private float scenarioWidth;
	private float scenarioHeight;
	private float bWidth;
	private float bHeight;
	private float bPosY;
	private EventBasedVisDebuggerEngine dbg;
	
	public BottleneckTestRunner(int pedsNumber, float scenarioWidth, float scenarioHeight, float bWidth, float bHeight, float bPosY){
		this.inputDir = Constants.FD_TEST_PATH+"bottleneck/"+(Math.round(bWidth * 10) / 10f)+"/input";
		this.outputDir = Constants.FD_TEST_PATH+"bottleneck/"+(Math.round(bWidth * 10) / 10f)+"/output";
		this.scenarioWidth = scenarioWidth;
		this.scenarioHeight = scenarioHeight;
		this.bWidth = bWidth;
		this.bHeight = bHeight;
		this.bPosY = bPosY;
		this.pedsNumber = pedsNumber;
	}
	
	public void generateScenario() {
		
		
		Config c = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(c);
		
		Context contextCA = ContextGenerator.createAndSaveBottleneckContext(inputDir+"/CAScenario", scenarioWidth, scenarioHeight, bWidth, bHeight, bPosY);
		NetworkGenerator.createNetwork(scenario, contextCA);
		c.network().setInputFile(inputDir + "/network.xml.gz");

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

		PopulationGenerator.createPopulation(scenario, pedsNumber);
		
		new ConfigWriter(c).write(inputDir+ "/config.xml");
		new NetworkWriter(scenario.getNetwork()).write(c.network().getInputFile());
		new PopulationWriter(scenario.getPopulation(), scenario.getNetwork()).write(c.plans().getInputFile());
	}
	
	public void runSimulation() {
		
		Config c = ConfigUtils.loadConfig(inputDir+"/config.xml");
		Scenario scenario = ScenarioUtils.loadScenario(c);
		CAScenario scenarioCA = new CAScenario(inputDir+"/CAScenario");
		HybridNetworkBuilder.buildNetwork(scenarioCA.getCAEnvironment(Id.create("0", CAEnvironment.class)), scenarioCA);
		scenarioCA.connect(scenario);
		new NetworkWriter(scenario.getNetwork()).write(c.network().getInputFile());
		
		c.controler().setWriteEventsInterval(1);
		c.controler().setLastIteration(0);
		c.qsim().setEndTime(Constants.SIMULATION_DURATION);

		final Controler controller = new Controler(scenario);
		
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
				
//		controller.addOverridingModule(new AbstractModule() {
//			@Override
//			public void install() {
//				addEventHandlerBinding().toInstance(
//						new FlowAnalyzer(Constants.FD_TEST_PATH+"bottleneck/"+bWidth+"/output/flow_data.csv"));
//			}});
		
		controller.getEvents().addHandler(new FlowAnalyzer(outputDir));
		controller.getEvents().addHandler(new DensityAnalyzer(outputDir, scenarioCA.getCAEnvironment(Id.create("0",CAEnvironment.class)).getContext()));
		
		if (Constants.VIS) {
			dbg = new EventBasedVisDebuggerEngine(scenario);
			InfoBox iBox = new InfoBox(dbg, scenario);
			dbg.addAdditionalDrawer(iBox);
			controller.getEvents().addHandler(dbg);
		}
		
		controller.addControlerListener(this);
		controller.run();
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		if (dbg != null)
			dbg.startIteration(event.getIteration()); 
	}
}

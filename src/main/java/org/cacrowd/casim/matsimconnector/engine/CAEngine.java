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

package org.cacrowd.casim.matsimconnector.engine;

import org.cacrowd.casim.matsimconnector.events.CAEngineStepPerformedEvent;
import org.cacrowd.casim.matsimconnector.scenario.CAEnvironment;
import org.cacrowd.casim.matsimconnector.scenario.CAScenario;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.pedca.engine.SimulationEngine;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.interfaces.MobsimEngine;
import org.matsim.core.mobsim.qsim.qnetsimengine.CALink;
import org.matsim.core.mobsim.qsim.qnetsimengine.CAQLink;
import org.matsim.core.mobsim.qsim.qnetsimengine.QCALink;

import java.util.HashMap;
import java.util.Map;


public class CAEngine implements MobsimEngine{
	
	private final QSim qSim;
	private final Scenario scenario;
	private final CAScenario scenarioCA;
	private final Map<Id<CAEnvironment>, SimulationEngine> enginesCA;
	private final CAAgentFactory agentFactoryCA;
	
	private final Map<Id<Link>,QCALink> linkToQCALink = new HashMap<Id<Link>,QCALink>();
	private final Map<Id<Link>,CAQLink> linkToCAQLink = new HashMap<Id<Link>,CAQLink>();
	private final Map<Id<Link>, CALink> linkToCALink = new HashMap<Id<Link>,CALink>();
	private double simCATime;
	
	public CAEngine(QSim qSim, CAAgentFactory agentFactoryCA){
		this.qSim = qSim;
		this.simCATime = 0.0;
		this.scenario = qSim.getScenario();
		this.scenarioCA = (CAScenario) this.scenario.getScenarioElement(Constants.CASCENARIO_NAME);
		this.enginesCA = new HashMap <Id<CAEnvironment>, SimulationEngine>();
		this.agentFactoryCA = agentFactoryCA;
	}
		
	private void initGenerators(CAAgentFactory agentFactoryCA) {
		for (Id<CAEnvironment> key : this.enginesCA.keySet())
			agentFactoryCA.addAgentsGenerator(key, this.enginesCA.get(key).getAgentGenerator());		
	}

	private void generateCAEngines() {
		for(CAEnvironment environmentCA : this.scenarioCA.getEnvironments().values())
			createAndAddEngine(environmentCA);
	}

	@Override
	public void doSimStep(double time) {
		double stepDuration = Constants.CA_STEP_DURATION;
		//Log.log("------> BEGINNING STEPS AT "+time);
		for (; this.simCATime < time; this.simCATime+=stepDuration) {
			for (SimulationEngine engine : this.enginesCA.values()) {
				double currentTime = System.currentTimeMillis();
				engine.doSimStep(this.simCATime);		
				double afterTime = System.currentTimeMillis();
				qSim.getEventsManager().processEvent(new CAEngineStepPerformedEvent(this.simCATime, (float)(afterTime-currentTime), engine.getAgentGenerator().getContext().getPopulation().size()));
			}
		}
	}

	@Override
	public void onPrepareSim() {
		generateCAEngines();
		initGenerators(this.agentFactoryCA);
	}

	@Override
	public void afterSim() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInternalInterface(InternalInterface internalInterface) {
		// TODO Auto-generated method stub
		
	}
	
	private void createAndAddEngine(CAEnvironment environmentCA){
		SimulationEngine engine = new SimulationEngine(environmentCA.getContext());
		engine.setAgentMover(new CAAgentMover(this, environmentCA.getContext(),this.qSim.getEventsManager()));
		this.enginesCA.put(environmentCA.getId(), engine);
	}

	public void registerHiResLink(QCALink hiResLink) {
		this.linkToQCALink.put(hiResLink.getLink().getId(),hiResLink);
	}

	public void registerLowResLink(CAQLink lowResLink) {
		this.linkToCAQLink.put(lowResLink.getLink().getId(), lowResLink);
		//lowResLinks.add(lowResLink);
	}

	public void registerCALink(CALink linkCA) {
		this.linkToCALink.put(linkCA.getLink().getId(), linkCA);
	}
	
	public QCALink getQCALink(Id<Link> linkId){
		return this.linkToQCALink.get(linkId);
	}
	
	public CAQLink getCAQLink(Id<Link> linkId){
		return this.linkToCAQLink.get(linkId); 
	}

	public CALink getCALink(Id<Link> linkId) {
		return this.linkToCALink.get(linkId);
	}
	
}
